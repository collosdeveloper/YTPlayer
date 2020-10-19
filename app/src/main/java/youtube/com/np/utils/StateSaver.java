package youtube.com.np.utils;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import static youtube.com.utils.LogUtils.LOGD;
import static youtube.com.utils.LogUtils.LOGE;

/**
 * A way to save state to disk or in a in-memory map if it's just changing configurations (i.e. rotating the phone).
 */
public class StateSaver {
	private static final String TAG = StateSaver.class.getSimpleName();
	
	private static final ConcurrentHashMap<String, Queue<Object>> stateObjectsHolder = new ConcurrentHashMap<>();;
	private static final String CACHE_DIR_NAME = "state_cache";
	
	public static final String KEY_SAVED_STATE = "key_saved_state";
	
	private static String cacheDirPath;
	
	private StateSaver() {
		// No instance
	}
	
	/**
	 * Initialize the StateSaver, usually you want to call this in the Application class
	 *
	 * @param context used to get the available cache dir
	 */
	public static void init(Context context) {
		File externalCacheDir = context.getExternalCacheDir();
		if (externalCacheDir != null) cacheDirPath = externalCacheDir.getAbsolutePath();
		if (TextUtils.isEmpty(cacheDirPath)) cacheDirPath = context.getCacheDir().getAbsolutePath();
	}
	
	/**
	 * Used for describe how to save/read the objects.
	 * <p>
	 * Queue was chosen by its FIFO property.
	 */
	public interface WriteRead {
		/**
		 * Generate a changing suffix that will name the cache file,
		 * and be used to identify if it changed (thus reducing useless reading/saving).
		 *
		 * @return a unique value
		 */
		String generateSuffix();
		
		/**
		 * Add to this queue objects that you want to save.
		 */
		void writeTo(Queue<Object> objectsToSave);
		
		/**
		 * Poll saved objects from the queue in the order they were written.
		 *
		 * @param savedObjects queue of objects returned by {@link #writeTo(Queue)}
		 */
		void readFrom(@NonNull Queue<Object> savedObjects) throws Exception;
	}
	
	/**
	 * @see #tryToRestore(SavedState, WriteRead)
	 */
	public static SavedState tryToRestore(Bundle outState, WriteRead writeRead) {
		if (outState == null || writeRead == null) return null;
		
		SavedState savedState = outState.getParcelable(KEY_SAVED_STATE);
		if (savedState == null) return null;
		
		return tryToRestore(savedState, writeRead);
	}
	
	/**
	 * Try to restore the state from memory and disk, using the {@link StateSaver.WriteRead#readFrom(Queue)} from the writeRead.
	 */
	@Nullable
	private static SavedState tryToRestore(@NonNull SavedState savedState, @NonNull WriteRead writeRead) {
		LOGD(TAG, "tryToRestore() called with: savedState = [" + savedState + "], writeRead = [" + writeRead + "]");
		
		FileInputStream fileInputStream = null;
		try {
			Queue<Object> savedObjects = stateObjectsHolder.remove(savedState.getPrefixFileSaved());
			if (savedObjects != null) {
				writeRead.readFrom(savedObjects);
				LOGD(TAG, "tryToSave: reading objects from holder > " + savedObjects + ", stateObjectsHolder > " + stateObjectsHolder);
				return savedState;
			}
			
			File file = new File(savedState.getPathFileSaved());
			if (!file.exists()) {
				LOGD(TAG, "Cache file doesn't exist: " + file.getAbsolutePath());
				return null;
			}
			
			fileInputStream = new FileInputStream(file);
			ObjectInputStream inputStream = new ObjectInputStream(fileInputStream);
			// Noinspection unchecked
			savedObjects = (Queue<Object>) inputStream.readObject();
			if (savedObjects != null) {
				writeRead.readFrom(savedObjects);
			}
			
			return savedState;
		} catch (Exception e) {
			LOGE(TAG, "Failed to restore state", e);
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException ignored) {
				}
			}
		}
		return null;
	}
	
	/**
	 * @see #tryToSave(boolean, String, String, WriteRead)
	 */
	@Nullable
	public static SavedState tryToSave(boolean isChangingConfig, @Nullable SavedState savedState, Bundle outState, WriteRead writeRead) {
		@NonNull
		String currentSavedPrefix;
		if (savedState == null || TextUtils.isEmpty(savedState.getPrefixFileSaved())) {
			// Generate unique prefix
			currentSavedPrefix = System.nanoTime() - writeRead.hashCode() + "";
		} else {
			// Reuse prefix
			currentSavedPrefix = savedState.getPrefixFileSaved();
		}
		
		savedState = tryToSave(isChangingConfig, currentSavedPrefix, writeRead.generateSuffix(), writeRead);
		if (savedState != null) {
			outState.putParcelable(StateSaver.KEY_SAVED_STATE, savedState);
			return savedState;
		}
		
		return null;
	}
	
	/**
	 * If it's not changing configuration (i.e. rotating screen), try to write the state from {@link StateSaver.WriteRead#writeTo(Queue)}
	 * to the file with the name of prefixFileName + suffixFileName, in a cache folder got from the {@link #init(Context)}.
	 * <p>
	 * It checks if the file already exists and if it does, just return the path, so a good way to save is:
	 * <ul>
	 * <li> A fixed prefix for the file</li>
	 * <li> A changing suffix</li>
	 * </ul>
	 *
	 * @param isChangingConfig
	 * @param prefixFileName
	 * @param suffixFileName
	 * @param writeRead
	 */
	@Nullable
	private static SavedState tryToSave(boolean isChangingConfig, final String prefixFileName, String suffixFileName, WriteRead writeRead) {
		LOGD(TAG, "tryToSave() called with: isChangingConfig = [" + isChangingConfig + "], prefixFileName = [" + prefixFileName + "], suffixFileName = [" + suffixFileName + "], writeRead = [" + writeRead + "]");
		
		LinkedList<Object> savedObjects = new LinkedList<>();
		writeRead.writeTo(savedObjects);
		
		if (isChangingConfig) {
			if (savedObjects.size() > 0) {
				stateObjectsHolder.put(prefixFileName, savedObjects);
				return new SavedState(prefixFileName, "");
			} else {
				LOGD(TAG, "Nothing to save");
				return null;
			}
		}
		
		FileOutputStream fileOutputStream = null;
		try {
			File cacheDir = new File(cacheDirPath);
			if (!cacheDir.exists()) throw new RuntimeException("Cache dir does not exist > " + cacheDirPath);
			cacheDir = new File(cacheDir, CACHE_DIR_NAME);
			if (!cacheDir.exists()) {
				if(!cacheDir.mkdir()) {
					LOGE(TAG, "Failed to create cache directory " + cacheDir.getAbsolutePath());
					return null;
				}
			}
			
			if (TextUtils.isEmpty(suffixFileName)) suffixFileName = ".cache";
			File file = new File(cacheDir, prefixFileName + suffixFileName);
			if (file.exists() && file.length() > 0) {
				// If the file already exists, just return it
				return new SavedState(prefixFileName, file.getAbsolutePath());
			} else {
				// Delete any file that contains the prefix
				File[] files = cacheDir.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.contains(prefixFileName);
					}
				});
				for (File fileToDelete : files) {
					fileToDelete.delete();
				}
			}
			
			fileOutputStream = new FileOutputStream(file);
			ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream);
			outputStream.writeObject(savedObjects);
			
			return new SavedState(prefixFileName, file.getAbsolutePath());
		} catch (Exception e) {
			LOGE(TAG, "Failed to save state", e);
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException ignored) {
				}
			}
		}
		return null;
	}
	
	/**
	 * Delete the cache file contained in the savedState and remove any possible-existing value in the memory-cache.
	 */
	public static void onDestroy(SavedState savedState) {
		LOGD(TAG, "onDestroy() called with: savedState = [" + savedState + "]");
		
		if (savedState != null && !TextUtils.isEmpty(savedState.getPathFileSaved())) {
			stateObjectsHolder.remove(savedState.getPrefixFileSaved());
			try {
				// Noinspection ResultOfMethodCallIgnored
				new File(savedState.getPathFileSaved()).delete();
			} catch (Exception ignored) {
			}
		}
	}
	
	/**
	 * Clear all the files in cache (in memory and disk).
	 */
	public static void clearStateFiles() {
		LOGD(TAG, "clearStateFiles() called");
		
		stateObjectsHolder.clear();
		File cacheDir = new File(cacheDirPath);
		if (!cacheDir.exists()) return;
		
		cacheDir = new File(cacheDir, CACHE_DIR_NAME);
		if (cacheDir.exists()) {
			for (File file : cacheDir.listFiles()) file.delete();
		}
	}

    // ----- Inner -----
	
	/**
	 * Information about the saved state on the disk
	 */
	public static class SavedState implements Parcelable {
		private final String prefixFileSaved;
		private final String pathFileSaved;
		
		public SavedState(String prefixFileSaved, String pathFileSaved) {
			this.prefixFileSaved = prefixFileSaved;
			this.pathFileSaved = pathFileSaved;
		}
		
		protected SavedState(Parcel in) {
			prefixFileSaved = in.readString();
			pathFileSaved = in.readString();
		}
		
		@Override
		public String toString() {
			return getPrefixFileSaved() + " > " + getPathFileSaved();
		}
		
		@Override
		public int describeContents() {
			return 0;
		}
		
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(prefixFileSaved);
			dest.writeString(pathFileSaved);
		}
		
		@SuppressWarnings("unused")
		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}
			
			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
		
		/**
		 * Get the prefix of the saved file
		 * @return the file prefix
		 */
		public String getPrefixFileSaved() {
			return prefixFileSaved;
		}
		
		/**
		 * Get the path to the saved file
		 * @return the path to the saved file
		 */
		public String getPathFileSaved() {
			return pathFileSaved;
		}
	}
}
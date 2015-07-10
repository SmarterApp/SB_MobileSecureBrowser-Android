/********************************************************************************
* Educational Online Test Delivery System
* Copyright (c) 2015 American Institutes for Research
*
* Distributed under the AIR Open Source License, Version 1.0
* See accompanying file AIR-License-1_0.txt or at
* http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
*********************************************************************************/
package com.air.mobilebrowser;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import android.content.Context;

/**
 *	Utility class for managing files.
 */
public class AudioFileUtil {
	
	/**
	 * Remove all files from the cache directory.
	 * @param context the context to use
	 */
	public static void cleanDirectory(Context context, HashSet<String> filesToSkip) {
		File directory = new File(context.getFilesDir(), "recordings" + File.separator);
		directory.mkdirs();
		
		File[] dirContents = directory.listFiles();
		
		for(File file : dirContents) {
			if (!filesToSkip.contains(file.getAbsolutePath())) {
				file.delete();
			}
		}
	}
	
	public static File[] getDirectoyContents(Context context) {
		File directory = new File(context.getFilesDir(), "recordings" + File.separator);
		directory.mkdirs();
		return directory.listFiles();
	}
	
	public static File getCacheTempFile(Context context, String name, String ext) throws IOException {
		File directory = new File(context.getCacheDir(), "temp" + File.separator);
		directory.mkdirs();
		File f = File.createTempFile(name, ext, directory);
		f.deleteOnExit();
		return f;
	}
	
	public static File getFile(Context context, String name, String ext) throws IOException {
		File directory = new File(context.getFilesDir(), "recordings" + File.separator);
		directory.mkdirs();
		return File.createTempFile(name, ext, directory);
	}
	
	public static File getAbsFile(Context context, String filename) throws IOException {
		File directory = new File(context.getFilesDir(), "recordings" + File.separator);
		directory.mkdirs();
		return new File(directory, filename);
	}

	public static File getFile(Context context, String filename) {
		File directory = new File(context.getFilesDir(), "recordings" + File.separator);
		directory.mkdirs();
		return new File(directory, filename);
	}
	
}

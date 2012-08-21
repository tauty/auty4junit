/*
 * Copyright 2011 tetsuo.ohta[at]gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tetz42.test;

import static org.junit.Assert.*;
import static tetz42.util.ObjDumper4j.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import tetz42.clione.common.Util;
import tetz42.exception.FileNotFoundException;
import tetz42.exception.WrapException;

/**
 * Utility class for JUnit.
 *
 * @author tetz
 * @version 0.4.6
 */
public class Auty {

	public static final String CRLF = System.getProperty("line.separator");

	public static void swapFile(String path1, String path2) {
		swapFile(new File(path1), new File(path2));
	}

	public static void swapFile(File file1, File file2) {
		checkFileExists(file1, file2);
		String hiddenPath = getDirToHide(file1) + "/" + file1.getName();
		hideFile(file1);
		file2.renameTo(file1);
		new File(hiddenPath).renameTo(file2);
	}

	public static void hideFile(String path) {
		hideFile(new File(path));
	}

	public static void hideFile(File file) {
		checkFileExists(file);
		String dirToHidePath = getDirToHide(file);
		File dirToHide = new File(dirToHidePath);
		if (!dirToHide.exists())
			dirToHide.mkdirs();
		file.renameTo(new File(dirToHidePath + "/" + file.getName()));
	}

	public static void restoreFile(String path) {
		restoreFile(new File(path));
	}

	public static void restoreFile(File file) {
		File hiddenFile = new File(getDirToHide(file) + "/" + file.getName());
		if (!hiddenFile.exists())
			throw new FileNotFoundException(
					"The file specfied might not be hidden. The path is:"
							+ CRLF + "original path:" + file.getPath() + CRLF
							+ "hidden path:" + hiddenFile.getPath());
		hiddenFile.renameTo(file);
	}

	public static void checkFileExists(String... pathes) {
		for (String path : pathes)
			checkFileExists(new File(path));
	}

	public static void checkFileExists(File... files) {
		for (File file : files) {
			if (!file.exists())
				throw new FileNotFoundException(
						"The file specfied might not be exsists. The path is:"
								+ file.getPath());
		}
	}

	private static String getDirToHide(File file) {
		String parentPath = file.getParent();
		return parentPath == null ? "hide" : "hide/" + parentPath;
	}

	/**
	 * Assertion method for complicated object.<br>
	 * It provides you an easy test environment.<br>
	 *
	 * @param actual
	 * @param clazz
	 * @param expectedFileName
	 */
	public static void assertEqualsWithFile(Object actual, Class<?> clazz,
			String expectedFileName) {
		String actStr = dumper(actual).crlf().superSafe().classFlatten()
				.primitiveFirst().toString();
		try {
			Writer writer = null;
			try {
				File file = new File(genFilePath(clazz, expectedFileName));
				if (file.exists()) {
					String expected = loadFromStream(new FileInputStream(file));
					try {
						assertSameStrings(expected, actStr);
					} catch (AssertionError e) {
						file = new File(genFailPath(clazz, expectedFileName));
						writer = new FileWriter(file);
						writer.append(actStr).flush();
						throw e;
					}
				} else {
					writer = new FileWriter(file);
					writer.append(actStr).flush();
					fail("No file found. The actual string has been output to the path:"
							+ CRLF
							+ file.getPath()
							+ CRLF
							+ " The contents is below:" + CRLF + actStr);
				}
			} finally {
				if (writer != null)
					writer.close();
			}
		} catch (IOException e) {
			throw new WrapException(e.getMessage(), e);
		}
	}

	public static void assertEqualsWithFile(Object actual, Class<?> clazz,
			String expectedFileName, int... ignoreLineNo) {
		String actStr = dumper(actual).superSafe().classFlatten()
				.primitiveFirst().toString();
		try {
			Writer writer = null;
			try {
				File file = new File(genFilePath(clazz, expectedFileName));
				if (file.exists()) {
					String expected = loadFromStream(new FileInputStream(file));
					try {
						assertSameStrings(expected, actStr, ignoreLineNo);
					} catch (AssertionError e) {
						file = new File(genFailPath(clazz, expectedFileName));
						writer = new FileWriter(file);
						writer.append(actStr).flush();
						throw e;
					}
				} else {
					writer = new FileWriter(file);
					writer.append(actStr).flush();
					fail("No file found. The actual string has been output to the path:"
							+ CRLF
							+ file.getPath()
							+ CRLF
							+ " The contents of a file are as follows:" + CRLF + actStr);
				}
			} finally {
				if (writer != null)
					writer.close();
			}
		} catch (IOException e) {
			throw new WrapException(e.getMessage(), e);
		}

	}

	private static void assertSameStrings(String expected, String actStr,
			int... ignoreLineNo) {
		if (expected.equals(actStr))
			return; // OK!

		// Build NG Message
		HashSet<Integer> ignoreSet = new HashSet<Integer>();
		for (int i : ignoreLineNo)
			ignoreSet.add(i - 1);

		ArrayList<Integer> unmatched = new ArrayList<Integer>();
		String[] expecteds = expected.split(CRLF);
		String[] actuals = actStr.split(CRLF);
		StringBuilder sb = new StringBuilder();
		sb.append("Actual data does not match! Check the diff message below:")
				.append(CRLF).append("  expected -> '-', actual -> '+' ")
				.append(CRLF).append(CRLF);
		int i;
		for (i = 0; i < actuals.length && i < expecteds.length; i++) {
			if (actuals[i].equals(expecteds[i])) {
				sb.append(padZero5(i)).append("|").append(actuals[i]).append(
						CRLF);
			} else {
				sb.append(padZero5(i)).append("|-").append(expecteds[i])
						.append(CRLF);
				sb.append(padZero5(i)).append("|+").append(actuals[i]).append(
						CRLF);
				if (!ignoreSet.contains(i))
					unmatched.add(i);
			}
		}
		for (int j = i; j < expecteds.length; j++) {
			sb.append(padZero5(j)).append("|-").append(expecteds[j]).append(
					CRLF);
			if (!ignoreSet.contains(j))
				unmatched.add(j);
		}
		for (int j = i; j < actuals.length; j++) {
			sb.append(padZero5(j)).append("|+").append(actuals[j]).append(CRLF);
			if (!ignoreSet.contains(j))
				unmatched.add(j);
		}
		if (unmatched.size() == 0) // it means this assertion error was ignored
			return;

		sb.append(CRLF).append("You can ignore this assertion ").append(
				"error to append parameters at 'assertEqualsWithFile' ")
				.append("method like below:").append(CRLF);
		sb.append("assertEqualsWithFile(foo, getClass(), \"file_name\", ");
		for (int k = 0; k < unmatched.size(); k++) {
			if (k != 0)
				sb.append(", ");
			sb.append(unmatched.get(k) + 1);
		}
		sb.append(");").append(CRLF);

		fail(sb.toString());
	}

	private static String padZero5(int i) {
		StringBuilder sb = new StringBuilder(String.valueOf(i + 1));
		while (sb.length() < 5)
			sb.insert(0, "0");
		return sb.toString();
	}

	public static String loadFile(String path) {
		File file = new File(path);
		checkFileExists(file);
		String result;
		try {
			InputStream in = new FileInputStream(file);
			try {
				result = loadFromStream(in);
			} finally {
				in.close();
			}
		} catch (IOException e) {
			throw new WrapException(e.getMessage(), e);
		}
		return result;
	}

	private static String loadFromStream(InputStream in) throws IOException {
		if (in == null)
			return null;
		final int SIZE = 100;
		ArrayList<byte[]> list = new ArrayList<byte[]>();
		int result = 0;
		while (true) {
			byte[] b = new byte[SIZE];
			result = in.read(b);
			if (result == -1)
				break;
			if (result < SIZE) {
				byte[] rest = new byte[result];
				System.arraycopy(b, 0, rest, 0, result);
				list.add(rest);
				break;
			}
			list.add(b);
		}
		byte[] b_all = new byte[SIZE * (list.size() - 1)
				+ list.get(list.size() - 1).length];
		for (int i = 0; i < list.size(); i++) {
			System.arraycopy(list.get(i), 0, b_all, i * SIZE,
					list.get(i).length);
		}
		return new String(b_all);
	}

	private static String genFilePath(Class<?> clazz, String fileName) {
		return genFilePath(clazz, fileName, null);
	}

	private static String genFailPath(Class<?> clazz, String fileName) {
		return genFilePath(clazz, fileName, "failed");
	}

	private static String genFilePath(Class<?> clazz, String fileName,
			String failedPath) {
		ResourceBundle bundle;
		try {
			bundle = ResourceBundle.getBundle("auty");
		} catch (MissingResourceException ignore) {
			bundle = null;
		}
		String rootPath = getBundle(bundle, "expected_file.root.path", "test");
		// String subfolder = getBundle(bundle, "expected_file.subfolder",
		// "expected");
		String subfolder = !Util.isEmpty(failedPath) ? failedPath : getBundle(
				bundle, "expected_file.subfolder", "expected");
		String extention = getBundle(bundle, "expected_file.extention", "txt");

		String dirPath = rootPath
				+ "/"
				+ clazz.getPackage().getName().toLowerCase().replaceAll("\\.",
						"/") + "/" + subfolder + "/" + clazz.getSimpleName();
		File dir = new File(dirPath);
		if (!dir.exists())
			dir.mkdirs();
		return dirPath + "/" + fileName + "." + extention;
	}

	private static String getBundle(ResourceBundle bundle, String key,
			String defval) {
		if (bundle != null) {
			try {
				String s = bundle.getString(key);
				if (s != null)
					return s;
			} catch (MissingResourceException ignore) {
			}
		}
		return defval;
	}
}

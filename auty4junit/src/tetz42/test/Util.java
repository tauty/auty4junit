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
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import tetz42.exception.FileNotFoundException;
import tetz42.exception.WrapException;

/**
 * Utility class for JUnit.
 * 
 * @author tetz
 * @version 0.1.0
 */
public class Util {

	public static final String CRLF = System.getProperty("line.separator");

	public static void hideFile(String path) {
		File file = new File(path);
		if (!file.exists())
			throw new FileNotFoundException(
					"The file specfied might not be exsists. The path is:"
							+ path);
		String dirToHidePath = getDirToHide(file);
		File dirToHide = new File(dirToHidePath);
		if (!dirToHide.exists())
			dirToHide.mkdirs();
		file.renameTo(new File(dirToHidePath + "/" + file.getName()));
	}

	public static void restoreFile(String path) {
		File file = new File(path);
		File hiddenFile = new File(getDirToHide(file) + "/" + file.getName());
		if (!hiddenFile.exists())
			throw new FileNotFoundException(
					"The file specfied might not be hidden. The path is:"
							+ CRLF + "original path:" + path + CRLF
							+ "hidden path:" + hiddenFile.getPath());
		hiddenFile.renameTo(file);
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
		String actStr = dumper(actual).superSafe().toString();
		try {
			Writer writer = null;
			try {
				File file = new File(genFilePath(clazz, expectedFileName));
				if (file.exists()) {
					String expected = loadFromStream(new FileInputStream(file));
					assertSameStrings(expected, actStr);
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

	private static void assertSameStrings(String expected, String actStr) {
		if (expected.equals(actStr))
			return; // OK!

		// Build NG Message
		String[] expecteds = expected.split(CRLF);
		String[] actuals = actStr.split(CRLF);
		StringBuilder sb = new StringBuilder();
		sb.append("Actual data doesn't match! Check the diff message below:")
				.append(CRLF).append("  expected -> '-', actual -> '+' ")
				.append(CRLF).append(CRLF);
		int i;
		for (i = 0; i < actuals.length && i < expecteds.length; i++) {
			if (actuals[i].equals(expecteds[i])) {
				sb.append(padZero5(i)).append("|").append(actuals[i])
						.append(CRLF);
			} else {
				sb.append(padZero5(i)).append("|-").append(expecteds[i])
						.append(CRLF);
				sb.append(padZero5(i)).append("|+").append(actuals[i])
						.append(CRLF);
			}
		}
		for (int j = i; j < expecteds.length; j++)
			sb.append(padZero5(j)).append("|-").append(expecteds[j])
					.append(CRLF);
		for (int j = i; j < actuals.length; j++)
			sb.append(padZero5(j)).append("|+").append(actuals[j]).append(CRLF);

		fail(sb.toString());
	}

	private static String padZero5(int i) {
		StringBuilder sb = new StringBuilder(String.valueOf(i + 1));
		while (sb.length() < 5)
			sb.insert(0, "0");
		return sb.toString();
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
		ResourceBundle bundle;
		try {
			bundle = ResourceBundle.getBundle("auty");
		} catch (MissingResourceException e) {
			bundle = null;
		}
		String rootPath = null;
		String extention = null;
		if (bundle != null) {
			rootPath = bundle.getString("expected_file.root.path");
			extention = bundle.getString("expected_file.extention");
		}
		if (rootPath == null)
			rootPath = "test";
		if (extention == null)
			extention = "txt";
		String dirPath = rootPath + "/"
				+ clazz.getName().toLowerCase().replaceAll("\\.", "/");
		File dir = new File(dirPath);
		if (!dir.exists())
			dir.mkdirs();
		return dirPath + "/" + fileName + "." + extention;
	}
}

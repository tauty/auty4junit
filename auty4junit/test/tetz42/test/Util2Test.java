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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static tetz42.test.Auty.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

/**
 * @author tetz
 */
public class Util2Test {

	@Test
	public void testDeleteFile() throws IOException {

		// generate tree
		new File("dir/to/remove").mkdirs();
		new File("dir/to/remove/file1").createNewFile();
		new File("dir/to/remove/file2").createNewFile();
		new File("dir/to/delete").mkdirs();
		new File("dir/to/delete/file1").createNewFile();
		new File("dir/to/delete/file2").createNewFile();
		new File("dir/to/file1").createNewFile();
		new File("dir/to/file2").createNewFile();
		
		assertTrue(new File("dir").exists());
		
		delTree("dir");

		assertFalse(new File("dir").exists());

	}

	@Test
	public void root() {
		assertTrue(new File("testfile.txt").exists());
		assertFalse(new File("hide/testfile.txt").exists());

		hideFile("testfile.txt");

		assertFalse(new File("testfile.txt").exists());
		assertTrue(new File("hide/testfile.txt").exists());

		restoreFile("testfile.txt");

		assertTrue(new File("testfile.txt").exists());
		assertFalse(new File("hide/testfile.txt").exists());
	}

	@Test
	public void testdir() {
		assertTrue(new File("testdir/testfile.txt").exists());
		assertFalse(new File("hide/testdir/testfile.txt").exists());

		hideFile("testdir/testfile.txt");

		assertFalse(new File("testdir/testfile.txt").exists());
		assertTrue(new File("hide/testdir/testfile.txt").exists());

		restoreFile("testdir/testfile.txt");

		assertTrue(new File("testdir/testfile.txt").exists());
		assertFalse(new File("hide/testdir/testfile.txt").exists());
	}

	@Test
	public void subdir() {
		assertTrue(new File("testdir/subdir/testfile.txt").exists());
		assertFalse(new File("hide/testdir/subdir/testfile.txt").exists());

		hideFile("testdir/subdir/testfile.txt");

		assertFalse(new File("testdir/subdir/testfile.txt").exists());
		assertTrue(new File("hide/testdir/subdir/testfile.txt").exists());

		restoreFile("testdir/subdir/testfile.txt");

		assertTrue(new File("testdir/subdir/testfile.txt").exists());
		assertFalse(new File("hide/testdir/subdir/testfile.txt").exists());
	}

	@Test
	public void swap() {
		String path1 = "testdir/subdir/file1.txt";
		String path2 = "testdir/subdir2/file2.txt";
		checkFileExists(path1, path2);

		assertThat(loadFile(path1), is("file1.txt"));
		assertThat(loadFile(path2), is("file2.txt"));

		swapFile(path1, path2);
		assertThat(loadFile(path1), is("file2.txt"));
		assertThat(loadFile(path2), is("file1.txt"));

		swapFile(path1, path2);
		assertThat(loadFile(path1), is("file1.txt"));
		assertThat(loadFile(path2), is("file2.txt"));
	}
}

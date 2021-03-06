/*
 * jGnash, a personal finance application
 * Copyright (C) 2001-2015 Craig Cavanaugh
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jgnash.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;

import org.junit.Test;

/**
 * File utilities test
 * 
 * @author Craig Cavanaugh
 */
public class FileUtilsTest {

    @Test
    public void fileExtensionStripTest() {
        assertTrue(FileUtils.fileHasExtension("text.txt"));
        assertTrue(FileUtils.fileHasExtension("text.txt.txt"));
        assertFalse(FileUtils.fileHasExtension("test"));
    }

    @Test
    public void strip() {
        assertEquals("database", FileUtils.stripFileExtension("database.h2.db"));
        assertEquals("database", FileUtils.stripFileExtension("database.db"));
    }

    @Test
    public void fileExtensionText() {
        assertEquals(FileUtils.getFileExtension("test.txt"), "txt");
    }

    @Test
    public void fileCopyToSelf() throws IOException {
        File tempFile = Files.createTempFile("jgnash-test", ".jdb").toFile();
        tempFile.deleteOnExit();

        String absolutePath = tempFile.getAbsolutePath();
        String testData = "42";

        // Write the data to a file
        writeTestData(testData, tempFile);
        checkTestData(testData, absolutePath);

        // Copy the file to itself: the file should not be emptied :)
        assertFalse(FileUtils.copyFile(new File(absolutePath), new File(absolutePath)));
    }

    private static void checkTestData(final String testdata, final String absolutepath) throws IOException {
        char[] buffer = new char[testdata.length()];

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(absolutepath))) {
            reader.read(buffer);
        }

        assertEquals(testdata, new String(buffer));
    }

    private static void writeTestData(final String testdata, final File tempfile) throws IOException {        
        try (OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(tempfile))) {
            os.write(testdata); 
        }               
    }
}
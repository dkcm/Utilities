/**
 * FileUtility.java  v0.2  2 April 2014 1:17:12 AM
 *
 * Copyright © 2014-2015 Daniel Kuan.  All rights reserved.
 */
package org.ikankechil.util;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for file operations.
 *
 * @author Daniel Kuan
 * @version 0.2
 */
public final class FileUtility {

  static final Logger logger = LoggerFactory.getLogger(FileUtility.class);

  private FileUtility() { /* disallow instantiation */ }

  /**
   * Delete an entire file tree.
   *
   * @param start
   * @throws IOException
   */
  public static final void deleteFileTree(final Path start) throws IOException {
    Files.walkFileTree(start, new SimpleFileVisitor<Path>() {

      @Override
      public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
          throws IOException {
        return deleteAndContinue(file);
      }

      @Override
      public FileVisitResult postVisitDirectory(final Path dir, final IOException ioE)
          throws IOException {
        if (ioE != null) { // directory iteration failed
          throw ioE;
        }
        return deleteAndContinue(dir);
      }

      private FileVisitResult deleteAndContinue(final Path path)
          throws IOException {
        if (Files.deleteIfExists(path)) {
          logger.info("File deleted: {}", path);
        }
        else {
          logger.info("File does not exist: {}", path);
        }

        return FileVisitResult.CONTINUE;
      }

    });
  }

  /**
   * Copy a file tree.
   *
   * @param source
   * @param target
   * @throws IOException
   */
  public static final void copy(final Path source, final Path target) throws IOException {
    Files.walkFileTree(source,
                       EnumSet.of(FileVisitOption.FOLLOW_LINKS),
                       Integer.MAX_VALUE,
                       new SimpleFileVisitor<Path>() {

      @Override
      public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
          throws IOException {
        final Path targetdir = target.resolve(source.relativize(dir));
        try {
          Files.copy(dir, targetdir);
        }
        catch (final FileAlreadyExistsException faeE) {
          if (!Files.isDirectory(targetdir)) {
            throw faeE;
          }
        }
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
          throws IOException {
        Files.copy(file, target.resolve(source.relativize(file)));
        logger.info("File copied: {}", file);
        return FileVisitResult.CONTINUE;
      }

    });
  }

}

/**
 *****************************************************************************
 * Copyright (c) 2017 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Arpit Rastogi - Initial Contribution
 *****************************************************************************
 */
/*
 * Directory Watcher to observe the NEW and modify event of directory specified in tmp_image_dir_path property.
 * also publishes the event to IoT using iot_event_for_img_base64 specified in properties file.
 */

package com.ibm.watson.WatsonVRTraining.util.images;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.JsonObject;
import com.ibm.watson.WatsonVRTraining.PredictionApp;
import com.ibm.watson.WatsonVRTraining.util.AppConstants;
 
/**
 * Example to watch a directory (or tree) for changes to files.
 */
 
public class WatchDir {
 
    private final WatchService watcher;
    private final Map<WatchKey,Path> keys;
    private final boolean recursive;
    private boolean trace = false;
    public static BlockingQueue<URI> queue = new LinkedBlockingQueue<URI>();
 
    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }
 
    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        //WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        WatchKey key = dir.register(watcher, ENTRY_CREATE,ENTRY_MODIFY);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                System.out.format("register: %s\n", dir);
            } else {
                if (!dir.equals(prev)) {
                    System.out.format("update: %s -> %s\n", prev, dir);
                }
            }
        }
        keys.put(key, dir);
    }
 
    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException
            {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
 
    /**
     * Creates a WatchService and registers the given directory
     */
    public WatchDir(Path dir, boolean recursive) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey,Path>();
        this.recursive = recursive;
 
        if (recursive) {
            System.out.format("Scanning %s ...\n", dir);
            registerAll(dir);
            System.out.println("Done.");
        } else {
            register(dir);
        }
 
        // enable trace after initial registration
        this.trace = true;
    }
 
    /**
     * Process all events for keys queued to the watcher
     */
    public void processEvents() {
    	
        for (;;) {
 
            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }
 
            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }
 
            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();
 
                // TBD - provide example of how OVERFLOW event is handled
                if (kind == OVERFLOW) {
                    continue;
                }
 
                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);
 
                // print out event
                //System.out.format("%s: %s\n", event.kind().name(), child);
                
                if((child.toString().endsWith(".jpg") || child.toString().endsWith(".jpeg") || child.toString().endsWith(".png")) &&
                		child.toString().contains(AppConstants.vr_process_img_dir_path))
                {
            		/*JsonObject event_payload = new JsonObject();
            		event_payload.addProperty("img_base64",new Base64EncoderDecoder().encodeFileToBase64Binary(new File(child.toUri())));
            		event_payload.addProperty("img_id", new File(child.toUri()).getName());*/
            		//event_payload.addProperty("_id",new File(child.toUri()).getName());
            		//event_payload.addProperty("img_result_html",PredictionApp.getInstance().imgsvc.analyzeImageJSon(new File(child.toUri())));
            		try {
            			//put img for process in queue
            			System.out.println("putting in queue:"+child.toString());
						this.queue.put(child.toUri());
						PhotoCaptureFrame.getImageRemainingProcessingLabel().setText("REMAINIG IMAGES:"+WatchDir.queue.size());
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                	
                }
                
                if((child.toString().endsWith(".jpg") || child.toString().endsWith(".jpeg") || child.toString().endsWith(".png")) &&
                		child.toString().contains(AppConstants.vr_train_img_dir_path))
                {
                	//any watcher stuff
                }
 
                // if directory is created, and watching recursively, then
                // register it and its sub-directories
                if (recursive && (kind == ENTRY_CREATE)) {
                    try {
                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                            registerAll(child);
                        }
                    } catch (IOException x) {
                        // ignore to keep sample readbale
                    }
                }
            }
 
            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);
 
                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
        
    }
 
	static void usage() {
        System.err.println("usage: java WatchDir [-r] dir");
        System.exit(-1);
    }
 
}

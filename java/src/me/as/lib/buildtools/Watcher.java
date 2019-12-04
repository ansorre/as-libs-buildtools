/*
 * Copyright 2019 Antonio Sorrentini
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package me.as.lib.buildtools;


import me.as.lib.buildtools.io.JavaBundleSpecs;
import me.as.lib.core.concurrent.SimpleSynchro;
import me.as.lib.core.concurrent.ThreadExtras;
import me.as.lib.core.extra.BoxFor2;
import me.as.lib.core.lang.ArrayExtras;
import me.as.lib.core.system.FileSystemExtras;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.System.currentTimeMillis;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static me.as.lib.core.log.DefaultTraceLevels.DEBUG;
import static me.as.lib.core.log.LogEngine.logOut;
import static me.as.lib.core.system.FileSystemExtras.isFile;
import static me.as.lib.core.system.FileSystemExtras.mergePath;


public class Watcher
{
 public static final long latencyMillis = 120;

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 final AtomicBoolean inWaiting=new AtomicBoolean(false);
 final AtomicLong lastChangeMillis=new AtomicLong();
 final SimpleSynchro ss=new SimpleSynchro();
 final SimpleSynchro innerSs=new SimpleSynchro();
 final ArrayList<String> changed=new ArrayList<>();
 final ArrayList<String> changedAndFireable=new ArrayList<>();
 final HashMap<String, JavaBundleSpecs> mappedBundleSpecs=new HashMap<>();
 final List<String> yetWatching=new ArrayList<>();

 private WatchService watcher;



 public void waitChanges()
 {
  if (inWaiting.compareAndExchange(false, true))
   throw new RuntimeException("This is yet waiting!");

  ss.waitFor();

  inWaiting.set(false);

  logOut.println("");
  logOut.println(DEBUG, DEBUG+" file system changes occurred!");
 }


 public List<BoxFor2<String, JavaBundleSpecs>> getChanged()
 {
  if (inWaiting.get())
   return null;

  synchronized (changedAndFireable)
  {
   ArrayList<BoxFor2<String, JavaBundleSpecs>> res=null;

   if (changedAndFireable.size()>0)
   {
    res=new ArrayList<>();

    for (String path : changedAndFireable)
    {
     String dir=FileSystemExtras.getDirAndFilename(path)[0];
     res.add(new BoxFor2<>(dir, mappedBundleSpecs.get(dir)));
    }
   }

   return res;
  }
 }


 private void addToChanged(String fpath)
 {
  lastChangeMillis.set(currentTimeMillis());

  synchronized (changed)
  {
   if (!changed.contains(fpath))
    changed.add(fpath);
  }

  innerSs.signalAll();
 }

 private void assisterMain()
 {
  do
  {
   innerSs.waitFor();
   long diff=currentTimeMillis()-lastChangeMillis.get();

   if (diff>=latencyMillis)
   {
    boolean fire=false;

    synchronized (changed)
    {
     if (changed.size()>0)
     {
      synchronized (changedAndFireable)
      {
       for (String fpath : changed)
       {
        if (!changedAndFireable.contains(fpath))
         changedAndFireable.add(fpath);
       }

       fire=changedAndFireable.size()>0;
      }

      changed.clear();
     }
    }

    if (fire) ss.signalAll();
   }
   else
   {
    boolean waitAndSignal;

    synchronized (changed)
    {
     waitAndSignal=(changed.size()>0);
    }

    if (waitAndSignal)
    {
     ThreadExtras.sleep(latencyMillis-diff+5);
     innerSs.signalAll();
    }
   }
  } while (true);

 }


 private void watcherMain()
 {
  WatchKey key=null;

  while (true)
  {
   try
   {
    key=watcher.take();
    List<WatchEvent<?>> list=key.pollEvents();
    Path dir=(Path)key.watchable();

    int t, len=ArrayExtras.length(list);

    for (t=0;t<len;t++)
    {
     WatchEvent event=list.get(t);
     String fpath=mergePath(dir.toString(), event.context().toString());

     if (isFile(fpath) ||
      event.kind()==ENTRY_DELETE ||
      event.kind()==ENTRY_CREATE)
     {
      addToChanged(fpath);
     }
    }
   }
   catch (InterruptedException x)
   {
    throw new RuntimeException(x);
   }
   finally
   {
    if (key!=null)
     key.reset();
   }
  }
 }



 public void addToWatching(String path, JavaBundleSpecs bundleSpecs)
 {
  if (!yetWatching.contains(path))
  {
   yetWatching.add(path);

   try
   {
    Path p=Path.of(path);

    if (watcher==null)
    {
     watcher=FileSystems.getDefault().newWatchService();
     ThreadExtras.executeOnAnotherThread(false, this::assisterMain);
     ThreadExtras.executeOnAnotherThread(false, this::watcherMain);
    }

    mappedBundleSpecs.put(path, bundleSpecs);
    p.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
   }
   catch (IOException io)
   {
    throw new me.as.lib.core.io.IOException(io);
   }
  }
 }


}

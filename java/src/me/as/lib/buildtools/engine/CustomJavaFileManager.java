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

package me.as.lib.buildtools.engine;


import me.as.lib.buildtools.io.CompiledJVMBytecode;
import me.as.lib.core.StillUnimplemented;
import me.as.lib.core.extra.BoxFor2;
import me.as.lib.core.extra.BoxFor3;
import me.as.lib.core.system.FileInfo;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public abstract class CustomJavaFileManager<I extends FileInfo> extends ForwardingJavaFileManager<StandardJavaFileManager>
{

 protected CustomJavaFileManager(JavaFileManager fileManager)
 {
  super((StandardJavaFileManager)fileManager);
 }

 public void setLocation(Location location, Iterable<? extends File> files)
 {
  try
  {
   fileManager.setLocation(location, files);
  }
  catch (Throwable tr)
  {
   throw new RuntimeException(tr);
  }
 }



 public JavaFileObject super_getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException
 {
  return super.getJavaFileForOutput(location, className, kind, sibling);
 }


 public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException
 {
//  return fileManager.getJavaFileForOutput(location, className, kind, sibling);
  try
  {
   return new CompiledJVMBytecode(className);
  }
  catch (Exception e)
  {
   throw new RuntimeException("Error while creating in-memory output file for "+className, e);
  }
 }


 public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException
 {
  throw new StillUnimplemented();
//  return fileManager.getFileForOutput(location, packageName, relativeName, sibling);
 }


 protected abstract BoxFor2<File, String> getFileAndWholePath(I eFI);

 public BoxFor3<List<CustomJavaFileObject>, List<I>, HashMap<String, CustomJavaFileObject>> getSourcesAndResources(List<I> sources)
 {
  ArrayList<I> resourcesRes=new ArrayList<>();
  List<File> files=new ArrayList<>();
  HashMap<String, I> map=new HashMap<>();

  for (I fi : sources)
  {
   if (fi.relativePath.endsWith(".java"))
   {
    BoxFor2<File, String> nb2=getFileAndWholePath(fi);
    File f=nb2.element1;
    files.add(f);
    map.put(nb2.element2, fi);
   }
   else
    if (fi.relativePath.endsWith(".class"))
     throw new StillUnimplemented();
    else
     resourcesRes.add(fi);
  }

  Iterable<? extends JavaFileObject> orig=fileManager.getJavaFileObjectsFromFiles(files);
  ArrayList<CustomJavaFileObject> sourcesRes=new ArrayList<>();
  HashMap<String, CustomJavaFileObject> mappedFileObjects=new HashMap<>();

  for (JavaFileObject jfo : orig)
  {
   String wtf=jfo.getName();
   CustomJavaFileObject cjfo=new CustomJavaFileObject(jfo, map.get(wtf));
   mappedFileObjects.put(cjfo.getName(), cjfo);
   sourcesRes.add(cjfo);
  }

  return new BoxFor3<>(sourcesRes, resourcesRes, mappedFileObjects);
 }



}

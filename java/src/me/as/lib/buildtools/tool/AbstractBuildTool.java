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

package me.as.lib.buildtools.tool;


import me.as.lib.buildtools.io.JavaBundleSpecs;
import me.as.lib.minicli.CommandLineHandler;
import me.as.lib.core.lang.ArrayExtras;
import me.as.lib.core.report.Problems;
import me.as.lib.core.system.FileSystemExtras;

import java.util.List;

import static me.as.lib.core.lang.StringExtras.isBlank;
import static me.as.lib.core.lang.StringExtras.isNotBlank;
import static me.as.lib.core.log.DefaultTraceLevels.INFO;
import static me.as.lib.core.log.LogEngine.logOut;
import static me.as.lib.core.system.FileSystemExtras.getCanonicalPath;
import static me.as.lib.core.system.FileSystemExtras.isDirectory;
import static me.as.lib.core.system.FileSystemExtras.mergePath;
import static me.as.lib.minicli.CommandLineHandler.useHelp;


public abstract class AbstractBuildTool<T>
{

 protected boolean canRunWithoutArgsAndWithoutOptions=false;

 protected String configFile;

 protected String verbosity;

 protected String workingDirectory;

 protected CommandLineHandler commandLineHandler;

 protected Problems problems;


 public abstract String getDefaultConfigFile();


 public void printUsageAndExit()
 {
  problems.addShowStopperNoPrefix(useHelp);
 }

 public Problems getProblems()
 {
  return problems;
 }

 public String getConfigFile()
 {
  return configFile;
 }

 public T setConfigFile(String configFile)
 {
  this.configFile=configFile;
  return (T)this;
 }

 public String getVerbosity()
 {
  return verbosity;
 }

 public T setVerbosity(String verbosity)
 {
  this.verbosity=verbosity;
  return (T)this;
 }

 public String getWorkingDirectory()
 {
  return workingDirectory;
 }

 public T setWorkingDirectory(String workingDirectory)
 {
  this.workingDirectory=workingDirectory;
  return (T)this;
 }

 public CommandLineHandler getCommandLineHandler()
 {
  return commandLineHandler;
 }

 public T setCommandLineHandler(CommandLineHandler commandLineHandler)
 {
  this.commandLineHandler=commandLineHandler;
  return (T)this;
 }




 protected void invalidDir(String whatDir, String whatValue)
 {
  problems.addShowStopper("Invalid "+whatDir+" '"+whatValue+"'. This is not a directory");
 }


 protected String getCanonicalPathForWorkingDirectory(String path, String defaultPath)
 {
  if (isBlank(path))
   if (isNotBlank(defaultPath)) path=mergePath(workingDirectory, defaultPath);
   else path=null;
  else
   if (path.startsWith(".\\") || path.startsWith("./"))
    path=mergePath(workingDirectory, path);

  if (isNotBlank(path)) path=getCanonicalPath(path);
  else path=null;

  return path;
 }



 protected void adjustJavaBundleSpecs(List<JavaBundleSpecs> jbss)
 {
  int t, len=ArrayExtras.length(jbss);

  if (len>0)
  {
   for (t=0;t<len;t++)
   {
    JavaBundleSpecs jbs=jbss.get(t);
    jbss.set(t, new JavaBundleSpecs(getCanonicalPathForWorkingDirectory(jbs.path, null),
     jbs.allowedPackages, jbs.excludedPackages, jbs.allowedResources, jbs.options));
   }
  }
 }




 protected void adjustParameters()
 {
  boolean configuredByFile=false;

  logOut.setTraceLevels(verbosity);

  // workingDirectory
  if (isBlank(workingDirectory))
   workingDirectory=getCanonicalPath(".");

  if (!isDirectory(workingDirectory))
  {
   invalidDir("workingDirectory", workingDirectory);
   return;
  }

  // configFile
  String defaultConfigFile=getDefaultConfigFile();
  boolean autoConfigFile=commandLineHandler.getNumberOfPassedOptions()==0;
  configFile=getCanonicalPathForWorkingDirectory(configFile, autoConfigFile ? (isNotBlank(defaultConfigFile) ? "./"+defaultConfigFile : null) : null);

  if (isNotBlank(configFile))
  {
   boolean worked=commandLineHandler.configureByFile(this, configFile);
   logOut.setTraceLevels(verbosity);
   if (commandLineHandler.getProblems().areThereShowStoppers()) return;

   if (autoConfigFile && worked)
    logOut.println(INFO, INFO+": read configuration from discovered '"+defaultConfigFile+"' in working directory.");

   if (worked)
   {
    configuredByFile=true;
    String dNf[]=FileSystemExtras.getDirAndFilename(configFile);
    workingDirectory=getCanonicalPath(dNf[0]);
   }

  }

  if (!canRunWithoutArgsAndWithoutOptions &&
      !configuredByFile &&
      ArrayExtras.length(commandLineHandler.getArgs())==0)
  {
   printUsageAndExit();
  }
 }






}

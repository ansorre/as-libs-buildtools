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

package me.as.lib.buildtools.io;


import me.as.lib.minicli.BasicCLIOptionHandlers;
import me.as.lib.minicli.CommandLineHandler;
import me.as.lib.minicli.Settings;
import me.as.lib.core.extra.BoxFor2;
import me.as.lib.core.lang.ArrayExtras;
import me.as.lib.core.report.Problems;
import me.as.lib.core.system.OSExtras;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.as.lib.core.lang.ClassExtras.setFieldValue_bruteForce;
import static me.as.lib.core.lang.StringExtras.betterTrimNl;
import static me.as.lib.core.lang.StringExtras.isNotBlank;
import static me.as.lib.core.lang.StringExtras.removeNullAndEmpty;


public class JavaBundleSpecsCLIHandler extends BasicCLIOptionHandlers.StringCLIOptionHandler
{

 protected void setFieldValue(CommandLineHandler handler, int argsPos, BoxFor2<Field, Settings> option)
 {
  setFieldValue_bruteForce(handler.getRunnerInstance(), option.element1, toJavaBundleSpecs(option.element2.name, operand, handler.getProblems()));
 }


 private static List<String> formatItems(List<String> current, String clause)
 {
  String s[]=removeNullAndEmpty(betterTrimNl(clause.split(",")));

  if (ArrayExtras.length(s)>0)
  {
   List<String> addings=Arrays.asList(s);

   if (ArrayExtras.length(current)==0)
    return addings;
   else
    current.addAll(addings);
  }

  return current;
 }


 private static List<JavaBundleSpecs> toJavaBundleSpecs(String argName, String operand, Problems problems)
 {
  List<JavaBundleSpecs> res=new ArrayList<>();

  if (isNotBlank(operand))
  {
   String splitters=";";
   if (OSExtras.isSomeUnix()) splitters+=":";

   String richPaths[]=operand.split("["+splitters+"]");
   int i, ilen, t, len=ArrayExtras.length(richPaths);

   for (t=0;t<len;t++)
   {
    String o[]=richPaths[t].split("\\{");
    String s[]=o[0].split("\\(");
    ilen=s.length;

    String path=s[0];
    List<String> allowedPackages=null;
    List<String> excludedPackages=null;
    List<String> allowedResources=null;
    List<String> options=null;

    if (o.length>1)
     options=formatItems(null, o[1].split("}")[0]);

    if (ilen>1)
    {
     for (i=1;i<ilen;i++)
     {
      if (s[i].startsWith("a)")) allowedPackages=formatItems(allowedPackages, s[i].substring(2));
      else if (s[i].startsWith("e)")) excludedPackages=formatItems(excludedPackages, s[i].substring(2));
      else if (s[i].startsWith("r)")) allowedResources=formatItems(allowedResources, s[i].substring(2));
      else
      {
       problems.addShowStopper("Parameter '"+argName+"' sub value '"+richPaths[t]+"' of '"+operand+
        "' is wrong. Paths can have (a) = allowed or (e) excluded additional clauses, not a '("+s[i]+"'");
       return null;
      }
     }
    }

    res.add(new JavaBundleSpecs(path, allowedPackages, excludedPackages, allowedResources, options));
   }
  }

  return res;
 }


/*

 public static void main(String args[])
 {
//  String test="uno:due;tre:quattro;cinque"; // linux
  //String test="uno;due;tre;quattro;cinque"; // windows
  String test=".\\java\\src(a)*pippo*,peppe(e);.\\java\\src(a)*genppo*,cirpe(e)a,b,c,d;uno;due;tre;quattro;cinque";

  Problems p=new Problems();
  List<JavaBundleSpecs> jbs=toJavaBundleSpecs("-sourcepath", test, p);

  p.printIfTheCase();

 }

*/


}

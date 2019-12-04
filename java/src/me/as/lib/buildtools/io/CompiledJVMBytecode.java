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

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;


public class CompiledJVMBytecode extends SimpleJavaFileObject
{
 private ByteArrayOutputStream baos=new ByteArrayOutputStream();
 private String className;


 public CompiledJVMBytecode(String className) throws Exception
 {
  super(new URI(className), Kind.CLASS);
  this.className=className;
 }

 public String getClassName()
 {
  return className;
 }

 @Override
 public OutputStream openOutputStream() throws IOException
 {
  return baos;
 }

 public byte[] getByteCode()
 {
  return baos.toByteArray();
 }


}

/*
 * Copyright (c) 1997, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package cz.kec.ora.xjc;

import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.Outline;
import org.xml.sax.ErrorHandler;

public class HelloWorldPlugin extends Plugin {

    static {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>I have been loaded!!");
    }

    public static void main(String[] args) {
        System.out.println(ModuleNameResolver.resolve());
    }

    public String getOptionName() {
        return "Xkec-hello-world";
    }

    public String getUsage() {
        return null;
    }

    public boolean run(Outline outline, Options options, ErrorHandler errorHandler) {
        //System.out.printf(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> HELLO WORLD MODULE='%s'", getModuleName(this.getClass()));
        System.out.printf(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> HELLO WORLD MODULE='%s'", ModuleNameResolver.resolve());//getModuleName(this.getClass()));
        return true;
    }

    private String getModuleName(Class<?> clazz) {
        ModuleAccessor moduleAccessor = ModuleNameResolver.generate();
        if (moduleAccessor.isModuleNamed(clazz)) {
            return moduleAccessor.getModuleName(clazz);
        }
        return "unnamed module";
    }
}
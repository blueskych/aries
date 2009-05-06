/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.blueprint.itests;

import java.util.Currency;
import java.util.Hashtable;

import org.apache.geronimo.blueprint.sample.Foo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.ops4j.pax.exam.CoreOptions.equinox;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.mavenConfiguration;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.OptionUtils;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.Bundle;
import org.osgi.service.blueprint.context.BlueprintContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

@RunWith(JUnit4TestRunner.class)
public class TestConfigAdmin extends AbstractIntegrationTest {

    @Test
    public void testStrategyNone() throws Exception {
        ConfigurationAdmin ca = getOsgiService(ConfigurationAdmin.class);
        Configuration cf = ca.getConfiguration("blueprint-sample-managed.none", null);
        Hashtable<String,String> props = new Hashtable<String,String>();
        props.put("a", "5");
        props.put("currency", "PLN");
        cf.update(props);

        Bundle bundle = getInstalledBundle("blueprint-sample");
        assertNotNull(bundle);
        bundle.start();

        BlueprintContext blueprintContext = getBlueprintContextForBundle("blueprint-sample", 5000);
        assertNotNull(blueprintContext);

        Foo foo = (Foo) blueprintContext.getComponent("none-managed");
        assertNotNull(foo);

        assertEquals(5, foo.getA());
        assertEquals(Currency.getInstance("PLN"), foo.getCurrency());

        props = new Hashtable<String,String>();
        props.put("a", "10");
        props.put("currency", "USD");
        cf.update(props);

        Thread.sleep(100);

        assertEquals(5, foo.getA());
        assertEquals(Currency.getInstance("PLN"), foo.getCurrency());
    }

    @Test
    public void testStrategyContainer() throws Exception {
        ConfigurationAdmin ca = getOsgiService(ConfigurationAdmin.class);
        Configuration cf = ca.getConfiguration("blueprint-sample-managed.container", null);
        Hashtable<String,String> props = new Hashtable<String,String>();
        props.put("a", "5");
        props.put("currency", "PLN");
        cf.update(props);

        Bundle bundle = getInstalledBundle("blueprint-sample");
        assertNotNull(bundle);
        bundle.start();

        BlueprintContext blueprintContext = getBlueprintContextForBundle("blueprint-sample", 5000);
        assertNotNull(blueprintContext);

        Foo foo = (Foo) blueprintContext.getComponent("container-managed");
        assertNotNull(foo);

        assertEquals(5, foo.getA());
        assertEquals(Currency.getInstance("PLN"), foo.getCurrency());

        props = new Hashtable<String,String>();
        props.put("a", "10");
        props.put("currency", "USD");
        cf.update(props);

        Thread.sleep(100);

        assertEquals(10, foo.getA());
        assertEquals(Currency.getInstance("USD"), foo.getCurrency());
    }

    @Test
    public void testStrategyComponent() throws Exception {
        ConfigurationAdmin ca = getOsgiService(ConfigurationAdmin.class);
        Configuration cf = ca.getConfiguration("blueprint-sample-managed.component", null);
        Hashtable<String,String> props = new Hashtable<String,String>();
        props.put("a", "5");
        props.put("currency", "PLN");
        cf.update(props);

        Bundle bundle = getInstalledBundle("blueprint-sample");
        assertNotNull(bundle);
        bundle.start();

        BlueprintContext blueprintContext = getBlueprintContextForBundle("blueprint-sample", 5000);
        assertNotNull(blueprintContext);

        Foo foo = (Foo) blueprintContext.getComponent("component-managed");
        assertNotNull(foo);

        assertEquals(5, foo.getA());
        assertEquals(Currency.getInstance("PLN"), foo.getCurrency());

        props = new Hashtable<String,String>();
        props.put("a", "10");
        props.put("currency", "USD");
        cf.update(props);

        Thread.sleep(100);

        assertEquals(5, foo.getA());
        assertEquals(Currency.getInstance("PLN"), foo.getCurrency());
        assertNotNull(foo.getProps());
        assertEquals("10", foo.getProps().get("a"));
        assertEquals("USD", foo.getProps().get("currency"));
    }

    @org.ops4j.pax.exam.junit.Configuration
    public static Option[] configuration() {
        Option[] options = options(
            // Log
            mavenBundle("org.ops4j.pax.logging", "pax-logging-api"),
            mavenBundle("org.ops4j.pax.logging", "pax-logging-service"),
            // Felix Config Admin
            mavenBundle("org.apache.felix", "org.apache.felix.configadmin"),
            // Felix mvn url handler
            mavenBundle("org.ops4j.pax.url", "pax-url-mvn"),


            // this is how you set the default log level when using pax logging (logProfile)
            systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("INFO"),

            // Bundles
            mavenBundle("org.apache.geronimo", "blueprint-bundle"),
            mavenBundle("org.apache.geronimo", "blueprint-sample").noStart(),

            felix(), equinox() //, knopflerfish()
        );

        // use config generated by the Maven plugin (until PAXEXAM-62/64 get resolved)
        if (TestConfigAdmin.class.getClassLoader().getResource("META-INF/maven/paxexam-config.args") != null) {
            options = OptionUtils.combine(options, mavenConfiguration());
        }

        return options;
    }

}
package net.apnic.rdap.conformance;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;

import net.apnic.rdap.conformance.Specification;
import net.apnic.rdap.conformance.Context;
import net.apnic.rdap.conformance.Test;
import net.apnic.rdap.conformance.test.domain.BadRequest;
import net.apnic.rdap.conformance.test.domain.Standard;
import net.apnic.rdap.conformance.Result;
import net.apnic.rdap.conformance.specification.ObjectClass;

class Application
{
    private static String getJarName()
    {
        String jar_name = "rdap-conformance.jar";
        try {
            jar_name =
                new java.io.File(Context.class.getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .getPath())
                        .getName();
        } catch (Exception e) {}
        return jar_name;
    }

    public static void main(String[] args)
    {
        if (args.length != 1) {
            System.out.println("Usage: java -jar " +
                               getJarName() + 
                               " <configuration-path>");
            System.exit(10);
        }

        String path = args[0];
        Specification s = null;
        try {
            s = Specification.fromPath(path);
        } catch (Exception e) {
            System.out.println("Unable to load specification " +
                               "path (" + path + "): " +
                               e.toString());
            System.exit(1);
        }
        if (s == null) {
            System.out.println("Specification (" + path + ") is empty.");
            System.exit(1);
        }

        HttpClient hc = HttpClientBuilder.create().build();
        Context c = new Context();
        c.setHttpClient(hc);
        c.setSpecification(s);

        List<String> object_types = new ArrayList<String>(
            Arrays.asList("ip", "nameserver", "autnum",
                          "entity", "domain")
        );

        List<Test> tests = new ArrayList();

        for (String object_type : object_types) {
            ObjectClass oc = s.getObjectClass(object_type);
            if ((oc != null)
                    && (!s.getObjectClass(object_type).isSupported())) {
                tests.add(new net.apnic.rdap.conformance.test.common.NotFound(
                            object_type)
                         );
            }
        }

        ObjectClass oc_ip = s.getObjectClass("ip");
        if ((oc_ip != null) && (oc_ip.isSupported())) {
            tests.add(new net.apnic.rdap.conformance.test.ip.BadRequest());
            List<String> exists = oc_ip.getExists();
            for (String e : exists) {
                tests.add(new net.apnic.rdap.conformance.test.ip.Standard(e));
            }
            List<String> not_exists = oc_ip.getNotExists();
            for (String e : not_exists) {
                tests.add(new net.apnic.rdap.conformance.test.common.NotFound(
                            "ip/" + e
                         ));
            }
        }

        ObjectClass oc_an = s.getObjectClass("autnum");
        if ((oc_an != null) && (oc_an.isSupported())) {
            tests.add(new net.apnic.rdap.conformance.test.autnum.BadRequest());
            List<String> exists = oc_an.getExists();
            for (String e : exists) {
                tests.add(
                    new net.apnic.rdap.conformance.test.autnum.Standard(e)
                );
            }
            List<String> not_exists = oc_an.getNotExists();
            for (String e : not_exists) {
                tests.add(new net.apnic.rdap.conformance.test.common.NotFound(
                            "autnum/" + e
                         ));
            }
        }

        ObjectClass oc_ns = s.getObjectClass("nameserver");
        if ((oc_ns != null) && (oc_ns.isSupported())) {
            tests.add(
                new net.apnic.rdap.conformance.test.nameserver.BadRequest()
            );
            List<String> exists = oc_ns.getExists();
            for (String e : exists) {
                tests.add(
                    new net.apnic.rdap.conformance.test.nameserver.Standard(e)
                );
            }
            List<String> not_exists = oc_ns.getNotExists();
            for (String e : not_exists) {
                tests.add(new net.apnic.rdap.conformance.test.common.NotFound(
                            "nameserver/" + e
                         ));
            }
        }

        ObjectClass oc_en = s.getObjectClass("entity");
        if ((oc_en != null) && (oc_en.isSupported())) {
            List<String> exists = oc_en.getExists();
            for (String e : exists) {
                tests.add(
                    new net.apnic.rdap.conformance.test.entity.Standard(e)
                );
            }
            List<String> not_exists = oc_en.getNotExists();
            for (String e : not_exists) {
                tests.add(new net.apnic.rdap.conformance.test.common.NotFound(
                           "entity/" + e
                          ));
            }
            /* That the entity handle happens to be an IP address should
               not cause a 400 to be returned. */
            tests.add(new net.apnic.rdap.conformance.test.common.NotFound(
                           "entity/1.2.3.4"
                      ));
        }

        ObjectClass oc_dom = s.getObjectClass("domain");
        if ((oc_dom != null) && (oc_dom.isSupported())) {
            tests.add(new net.apnic.rdap.conformance.test.domain.BadRequest());
            List<String> exists = oc_dom.getExists();
            for (String e : exists) {
                tests.add(
                    new net.apnic.rdap.conformance.test.domain.Standard(e)
                );
            }
            List<String> not_exists = oc_dom.getNotExists();
            for (String e : not_exists) {
                tests.add(new net.apnic.rdap.conformance.test.common.NotFound(
                            "domain/" + e
                         )); 
            }
        }

        for (Test t : tests) {
            t.run(c);
            c.flushResults();
        }
    }
}
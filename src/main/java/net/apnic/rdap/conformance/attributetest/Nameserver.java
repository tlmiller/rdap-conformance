package net.apnic.rdap.conformance.attributetest;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.apnic.rdap.conformance.Utils;
import net.apnic.rdap.conformance.Result;
import net.apnic.rdap.conformance.Result.Status;
import net.apnic.rdap.conformance.Context;
import net.apnic.rdap.conformance.AttributeTest;
import net.apnic.rdap.conformance.SearchTest;
import net.apnic.rdap.conformance.valuetest.IPv4Address;
import net.apnic.rdap.conformance.valuetest.IPv6Address;

public class Nameserver implements SearchTest
{
    private String key = null;
    private String pattern = null;
    private boolean checkUnknown = false;
    Set<String> knownAttributes = null;

    public Nameserver(boolean argCheckUnknown)
    {
        checkUnknown = argCheckUnknown;
    }

    public void setSearchDetails(String argKey, String argPattern)
    {
        key = argKey;
        pattern = argPattern;
    }

    public boolean run(Context context, Result proto,
                       Map<String, Object> data)
    {
        Result nr = new Result(proto);
        nr.setCode("content");
        nr.setDocument("draft-ietf-weirds-json-response-07");
        nr.setReference("6.2");

        SearchTest domainNames = new DomainNames();
        if (key != null) {
            domainNames.setSearchDetails(key, pattern);
        }

        List<AttributeTest> tests =
            new ArrayList<AttributeTest>(Arrays.asList(
                new ScalarAttribute("handle"),
                domainNames,
                new StandardObject()
            ));

        knownAttributes = new HashSet<String>();

        boolean ret = true;
        for (AttributeTest test : tests) {
            boolean res = test.run(context, nr, data);
            if (!res) {
                ret = false;
            }
            knownAttributes.addAll(test.getKnownAttributes());
        }

        Map<String, Object> ipAddresses =
            Utils.getMapAttribute(context, nr, "ipAddresses",
                                  Status.Notification, data);
        if (ipAddresses != null) {
            Result nr2 = new Result(nr);
            nr2.addNode("ipAddresses");
            AttributeTest v4 = new ArrayAttribute(new IPv4Address(), "v4");
            AttributeTest v6 = new ArrayAttribute(new IPv6Address(), "v6");
            boolean v4res = v4.run(context, nr2, ipAddresses);
            boolean v6res = v6.run(context, nr2, ipAddresses);
            if (!v4res || !v6res) {
                ret = false;
            }
            knownAttributes.addAll(v4.getKnownAttributes());
            knownAttributes.addAll(v6.getKnownAttributes());
        }
        knownAttributes.add("ipAddresses");

        boolean ret2 = true;
        if (checkUnknown) {
            AttributeTest ua = new UnknownAttributes(knownAttributes);
            ret2 = ua.run(context, nr, data);
        }

        return (ret && ret2);
    }

    public Set<String> getKnownAttributes()
    {
        return knownAttributes;
    }
}
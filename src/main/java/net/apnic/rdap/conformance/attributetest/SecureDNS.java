package net.apnic.rdap.conformance.attributetest;

import java.util.Map;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Set;

import net.apnic.rdap.conformance.Utils;
import net.apnic.rdap.conformance.Result;
import net.apnic.rdap.conformance.Context;
import net.apnic.rdap.conformance.AttributeTest;
import net.apnic.rdap.conformance.valuetest.BooleanValue;
import net.apnic.rdap.conformance.valuetest.MaxSigLife;

public class SecureDNS implements AttributeTest
{
    private Set<String> knownAttributes = new HashSet<String>();

    public SecureDNS() {}

    public boolean run(Context context, Result proto,
                       Map<String, Object> data)
    {
        return Utils.runTestList(
            context, proto, data, knownAttributes, true,
            Arrays.asList(
                new ScalarAttribute("zoneSigned", new BooleanValue()),
                new ScalarAttribute("delegationSigned", new BooleanValue()),
                new ScalarAttribute("maxSigLife", new MaxSigLife()),
                new ArrayAttribute(new DsData(), "dsData"),
                new ArrayAttribute(new KeyData(), "keyData")
            )
        );
    }

    public Set<String> getKnownAttributes()
    {
        return knownAttributes;
    }
}
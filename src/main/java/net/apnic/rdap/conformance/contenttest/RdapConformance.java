package net.apnic.rdap.conformance.contenttest;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import net.apnic.rdap.conformance.Context;
import net.apnic.rdap.conformance.Result;
import net.apnic.rdap.conformance.Result.Status;
import net.apnic.rdap.conformance.ContentTest;

public class RdapConformance implements ContentTest
{
    public RdapConformance() {}

    public boolean run(Context context, Result proto, 
                       Object arg_data)
    {
        Result nr = new Result(proto);
        /* All attributes are optional, strictly speaking (json-response 3.2),
         * but for the attributes which aren't sensitive and will always be
         * available, absence will be treated as error. */
        nr.setCode("content");
        nr.addNode("rdapConformance");
        nr.setDocument("draft-ietf-weirds-json-response-06");
        nr.setReference("5.1");

        Map<String, Object> data;
        try {
            data = (Map<String, Object>) arg_data;
        } catch (ClassCastException e) {
            nr.setInfo("structure is invalid");
            nr.setStatus(Status.Failure);
            context.addResult(nr);
            return false;
        }

        Result nr1 = new Result(nr);
        nr1.setInfo("present");

        Object value = data.get("rdapConformance");
        if (value == null) {
            nr1.setStatus(Status.Failure);
            nr1.setInfo("not present");
            context.addResult(nr1);
            return false;
        } else {
            nr1.setStatus(Status.Success);
            context.addResult(nr1);
        }

        Result nr2 = new Result(nr);
        nr2.setInfo("is an array");

        List<Object> rcs;
        try { 
            rcs = (List<Object>) value;
        } catch (ClassCastException e) {
            nr2.setStatus(Status.Failure);
            nr2.setInfo("is not an array");
            context.addResult(nr2);
            return false;
        }

        nr2.setStatus(Status.Success);
        context.addResult(nr2);

        Result nr3 = new Result(nr);
        nr3.setInfo("contains rdap_level_0");

        if (rcs.contains((Object) "rdap_level_0")) {
            nr3.setStatus(Status.Success);
        } else {
            nr3.setStatus(Status.Failure);
            nr3.setInfo("does not contain rdap_level_0");
        }

        context.addResult(nr3);

        return true;
    }
}
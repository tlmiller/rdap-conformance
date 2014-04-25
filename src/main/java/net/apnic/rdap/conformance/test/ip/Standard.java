package net.apnic.rdap.conformance.test.ip;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.List;
import org.apache.http.conn.util.InetAddressUtils;

import com.google.common.collect.Sets;

import net.apnic.rdap.conformance.Result;
import net.apnic.rdap.conformance.Utils;
import net.apnic.rdap.conformance.Result.Status;
import net.apnic.rdap.conformance.Context;
import net.apnic.rdap.conformance.ContentTest;
import net.apnic.rdap.conformance.contenttest.StandardResponse;
import net.apnic.rdap.conformance.contenttest.UnknownAttributes;

public class Standard implements net.apnic.rdap.conformance.Test
{
    String ip = "";

    public Standard(String arg_ip)
    {
        ip = arg_ip;
    }

    private String processIpAddress(Context context, Result proto, 
                                    Map root, String key)
    {
        Result ipres = new Result(proto);
        String address = (String) root.get(key);
        if (address == null) {
            ipres.setStatus(Status.Failure);
            ipres.setInfo(key + " element not found");
            context.addResult(ipres);
        } else { 
            ipres.setStatus(Status.Success);
            ipres.setInfo(key + " element found");
            context.addResult(ipres);
            
            Result ipvalid = new Result(proto);
            ipvalid.setStatus(Status.Success);
            ipvalid.setInfo(key + " element is valid");
            if (!(InetAddressUtils.isIPv4Address(address)
                    || InetAddressUtils.isIPv6Address(address))) {
                ipvalid.setStatus(Status.Failure);
                ipvalid.setInfo(key + " element is not valid");
                context.addResult(ipvalid);
            } else {
                ipvalid.setStatus(Status.Success);
                ipvalid.setInfo(key + " element is valid");
                context.addResult(ipvalid);
            }
        }
        return address;
    }

    public boolean run(Context context)
    {
        List<Result> results = context.getResults();

        String bu = context.getSpecification().getBaseUrl();
        String path = bu + "/ip/" + ip;

        Result proto = new Result(Status.Notification, path,
                                  "ip.standard",
                                  "", "", "", "");
        proto.setCode("content");
        Result r = new Result(proto);
        r.setCode("response");
        Map root = Utils.standardRequest(context, path, r);
        if (root == null) {
            return false;
        }

        int version = 0;
        String start_address = 
            processIpAddress(context, proto, root, "startAddress");
        String end_address =
            processIpAddress(context, proto, root, "endAddress");
        if ((start_address != null) && (end_address != null)) {
            Result types_match = new Result(proto);
            types_match.setInfo("start and end address types match");
            types_match.setStatus(Status.Success);
            if (InetAddressUtils.isIPv4Address(start_address)
                    && InetAddressUtils.isIPv4Address(end_address)) {
                context.addResult(types_match);
                version = 4;
            } else if (InetAddressUtils.isIPv6Address(start_address)
                    && InetAddressUtils.isIPv6Address(end_address)) {
                context.addResult(types_match);
                version = 6;
            } else {
                types_match.setInfo(
                    "start and end address types do not match"
                );
                types_match.setStatus(Status.Failure);
                context.addResult(types_match);
            }
        }

        Result vres = new Result(proto);
        vres.addNode("ipVersion");
        String ipversion = Utils.castToString(root.get("ipVersion"));
        if (ipversion == null) {
            vres.setStatus(Status.Failure);
            vres.setInfo("not present");
            context.addResult(vres);
        } else {
            vres.setStatus(Status.Success);
            vres.setInfo("present");
            context.addResult(vres);
            Result vres2 = new Result(vres);
            int check_version = ipversion.equals("v4") ? 4
                              : ipversion.equals("v6") ? 6
                                                       : 0;
            if (check_version == 0) {
                vres2.setStatus(Status.Failure);
                vres2.setInfo("invalid");
                context.addResult(vres2);
            } else {
                vres2.setStatus(Status.Success);
                vres2.setInfo("valid");
                context.addResult(vres2);
                if (version != 0) {
                    Result vres3 = new Result(vres);
                    if (version == check_version) {
                        vres3.setStatus(Status.Success);
                        vres3.setInfo("matches address version");
                        context.addResult(vres3);
                    } else {
                        vres3.setStatus(Status.Failure);
                        vres3.setInfo("does not match address version");
                        context.addResult(vres3);
                    }
                }
            }
        }

        ContentTest srt = new StandardResponse();
        boolean ret = srt.run(context, proto, root);

        Set<String> known_attributes = new HashSet<String>();
        known_attributes.addAll(srt.getKnownAttributes());
        known_attributes.addAll(Sets.newHashSet(
            "startAddress", "endAddress", "handle", "ipVersion",
            "name", "type", "country"
        ));
        ContentTest ua = new UnknownAttributes(known_attributes);
        boolean ret2 = ua.run(context, proto, root);
        return (ret && ret2);
    }
}

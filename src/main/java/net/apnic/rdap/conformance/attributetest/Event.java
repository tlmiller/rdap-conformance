package net.apnic.rdap.conformance.attributetest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import net.apnic.rdap.conformance.Result;
import net.apnic.rdap.conformance.Result.Status;
import net.apnic.rdap.conformance.Context;
import net.apnic.rdap.conformance.Utils;
import net.apnic.rdap.conformance.AttributeTest;
import net.apnic.rdap.conformance.valuetest.EventAction;

/**
 * <p>Event class.</p>
 *
 * @author Tom Harrison <tomh@apnic.net>
 * @version 0.3-SNAPSHOT
 */
public final class Event implements AttributeTest {
    private boolean allowActor = true;

    private static final Set<String> EVENT_ACTIONS =
        Sets.newHashSet("registration",
                        "reregistration",
                        "last changed",
                        "expiration",
                        "deletion",
                        "reinstantiation",
                        "transfer",
                        "locked",
                        "unlocked");

    /**
     * <p>Constructor for Event.</p>
     *
     * @param argAllowActor a boolean.
     */
    public Event(final boolean argAllowActor) {
        allowActor = argAllowActor;
    }

    /**
     * <p>Constructor for Event.</p>
     */
    public Event() { }

    /** {@inheritDoc} */
    public boolean run(final Context context, final Result proto,
                       final Map<String, Object> data) {
        List<Result> results = context.getResults();

        Result nr = new Result(proto);
        nr.setCode("content");
        nr.setDocument("draft-ietf-weirds-json-response-07");
        nr.setReference("5.5");

        AttributeTest evt =
            new ScalarAttribute("eventAction", new EventAction());
        boolean evtres = evt.run(context, nr, data);

        DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser();

        boolean evdres = true;
        String eventDate =
            Utils.getStringAttribute(context, nr, "eventDate",
                                     Status.Failure, data);
        if (eventDate == null) {
            evdres = false;
        } else {
            Result edvr = new Result(nr);
            edvr.addNode("eventDate");
            edvr.setReference("4");
            DateTime dth = parser.parseDateTime(eventDate);
            if (dth != null) {
                edvr.setInfo("valid");
                edvr.setStatus(Status.Success);
                results.add(edvr);
            } else {
                edvr.setInfo("invalid");
                edvr.setStatus(Status.Failure);
                results.add(edvr);
                evdres = false;
            }
        }

        boolean eacres = true;
        String eventActor =
            Utils.getStringAttribute(context, nr, "eventActor",
                                     Status.Notification, data);
        if ((eventActor != null) && !allowActor) {
            Result eacr = new Result(nr);
            eacr.addNode("eventActor");
            eacr.setInfo("not permitted here");
            eacr.setStatus(Status.Failure);
            results.add(eacr);
            eacres = false;
        }

        AttributeTest lst = new Links();
        boolean lstres = lst.run(context, proto, data);

        return (evtres && evdres && eacres && lstres);
    }

    /**
     * <p>getKnownAttributes.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<String> getKnownAttributes() {
        return Sets.newHashSet("eventActor", "eventDate",
                               "eventAction");
    }
}

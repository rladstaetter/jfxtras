package jfxtras.scene.control.agenda.icalendar.editors.revisor;

import static org.junit.Assert.assertEquals;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import jfxtras.icalendarfx.VCalendar;
import jfxtras.icalendarfx.components.VEvent;
import jfxtras.icalendarfx.components.VPrimary;
import jfxtras.icalendarfx.properties.calendar.Version;
import jfxtras.icalendarfx.properties.component.change.DateTimeStamp;
import jfxtras.icalendarfx.properties.component.descriptive.Summary;
import jfxtras.icalendarfx.properties.component.recurrence.rrule.FrequencyType;
import jfxtras.icalendarfx.properties.component.recurrence.rrule.RecurrenceRuleValue;
import jfxtras.icalendarfx.properties.component.recurrence.rrule.byxxx.ByDay;
import jfxtras.scene.control.agenda.icalendar.ICalendarAgenda;
import jfxtras.scene.control.agenda.icalendar.ICalendarStaticComponents;
import jfxtras.scene.control.agenda.icalendar.agenda.AgendaTestAbstract;
import jfxtras.scene.control.agenda.icalendar.editors.ChangeDialogOption;
import jfxtras.scene.control.agenda.icalendar.editors.revisors.ReviserVEvent;
import jfxtras.scene.control.agenda.icalendar.editors.revisors.SimpleRevisorFactory;

public class ReviseOneTest
{
    @Test
    public void canEditOneRecurrence()
    {
        VCalendar mainVCalendar = new VCalendar();
        VEvent vComponentOriginal = ICalendarStaticComponents.getDaily1();
        mainVCalendar.addChild(vComponentOriginal);
        final List<VEvent> vComponents = mainVCalendar.getVEvents();
        VEvent vComponentEdited = new VEvent(vComponentOriginal);

        vComponentEdited.setSummary("Edited summary");
        Temporal startOriginalRecurrence = LocalDateTime.of(2016, 5, 16, 10, 0);
        Temporal startRecurrence = LocalDateTime.of(2016, 5, 16, 9, 0);
        Temporal endRecurrence = LocalDateTime.of(2016, 5, 16, 10, 30);

        ReviserVEvent reviser = ((ReviserVEvent) SimpleRevisorFactory.newReviser(vComponentEdited))
                .withDialogCallback((m) -> ChangeDialogOption.ONE)
                .withEndRecurrence(endRecurrence)
                .withStartOriginalRecurrence(startOriginalRecurrence)
                .withStartRecurrence(startRecurrence)
                .withVComponentCopyEdited(vComponentEdited)
                .withVComponentOriginal(vComponentOriginal);
        List<VCalendar> iTIPMessages = reviser.revise();

        iTIPMessages.forEach(inputVCalendar -> mainVCalendar.processITIPMessage(inputVCalendar));
        Collections.sort(vComponents, VPrimary.DTSTART_COMPARATOR);
//        System.out.println("vComponents:" + vComponents.size());
//        vComponents.forEach(System.out::println);
        VEvent myComponentIndividual = vComponents.get(1);
        
        String expectediTIPMessage =
                "BEGIN:VCALENDAR" + System.lineSeparator() +
                "METHOD:PUBLISH" + System.lineSeparator() +
                "PRODID:" + ICalendarAgenda.DEFAULT_PRODUCT_IDENTIFIER + System.lineSeparator() +
                "VERSION:" + Version.DEFAULT_ICALENDAR_SPECIFICATION_VERSION + System.lineSeparator() +
                "BEGIN:VEVENT" + System.lineSeparator() +
                "CATEGORIES:group05" + System.lineSeparator() +
                "DTSTART:20160516T090000" + System.lineSeparator() +
                "DTEND:20160516T103000" + System.lineSeparator() +
                "DESCRIPTION:Daily1 Description" + System.lineSeparator() +
                "SUMMARY:Edited summary" + System.lineSeparator() +
                myComponentIndividual.getDateTimeStamp().toString() + System.lineSeparator() +
                "UID:20150110T080000-004@jfxtras.org" + System.lineSeparator() +
                "ORGANIZER;CN=Papa Smurf:mailto:papa@smurf.org" + System.lineSeparator() +
                "RECURRENCE-ID:20160516T100000" + System.lineSeparator() +
                "SEQUENCE:1" + System.lineSeparator() +
                "END:VEVENT" + System.lineSeparator() +
                "END:VCALENDAR";
        String iTIPMessage = iTIPMessages.stream()
                .map(v -> v.toString())
                .collect(Collectors.joining(System.lineSeparator()));
        assertEquals(expectediTIPMessage, iTIPMessage);
        assertEquals(2, mainVCalendar.getVEvents().size());

        // 2nd edit - edit component with RecurrenceID (individual)
        VEvent vComponentEditedIndividual = new VEvent(myComponentIndividual);
        VEvent vComponentIndividualCopy = new VEvent(myComponentIndividual);
        
        vComponentEditedIndividual.setSummary("new summary");
        Temporal startOriginalRecurrence2 = LocalDateTime.of(2016, 5, 16, 9, 0);
        Temporal startRecurrence2 = LocalDateTime.of(2016, 5, 16, 12, 0);
        Temporal endRecurrence2 = LocalDateTime.of(2016, 5, 16, 13, 0);

        ReviserVEvent reviser2 = ((ReviserVEvent) SimpleRevisorFactory.newReviser(vComponentIndividualCopy))
                .withDialogCallback((m) -> null) // no dialog required
                .withEndRecurrence(endRecurrence2)
                .withStartOriginalRecurrence(startOriginalRecurrence2)
                .withStartRecurrence(startRecurrence2)
                .withVComponentCopyEdited(vComponentEditedIndividual)
                .withVComponentOriginal(vComponentIndividualCopy);
        iTIPMessages = reviser2.revise();
        iTIPMessages.forEach(inputVCalendar -> mainVCalendar.processITIPMessage(inputVCalendar));
        
        expectediTIPMessage =
                "BEGIN:VCALENDAR" + System.lineSeparator() +
                "METHOD:REQUEST" + System.lineSeparator() +
                "PRODID:" + ICalendarAgenda.DEFAULT_PRODUCT_IDENTIFIER + System.lineSeparator() +
                "VERSION:" + Version.DEFAULT_ICALENDAR_SPECIFICATION_VERSION + System.lineSeparator() +
                "BEGIN:VEVENT" + System.lineSeparator() +
                "CATEGORIES:group05" + System.lineSeparator() +
                "DTSTART:20160516T120000" + System.lineSeparator() +
                "DTEND:20160516T130000" + System.lineSeparator() +
                "DESCRIPTION:Daily1 Description" + System.lineSeparator() +
                "SUMMARY:new summary" + System.lineSeparator() +
                vComponentEditedIndividual.getDateTimeStamp().toString() + System.lineSeparator() +
                "UID:20150110T080000-004@jfxtras.org" + System.lineSeparator() +
                "ORGANIZER;CN=Papa Smurf:mailto:papa@smurf.org" + System.lineSeparator() +
                "RECURRENCE-ID:20160516T100000" + System.lineSeparator() +
                "SEQUENCE:2" + System.lineSeparator() +
                "END:VEVENT" + System.lineSeparator() +
                "END:VCALENDAR";
        iTIPMessage = iTIPMessages.stream()
                .map(v -> v.toString())
                .collect(Collectors.joining(System.lineSeparator()));
        assertEquals(expectediTIPMessage, iTIPMessage);
    }
     
    @Test
    public void canChangeTimeBasedToWholeDayOne()
    {
        VCalendar mainVCalendar = new VCalendar();
        VEvent vComponentOriginal = ICalendarStaticComponents.getDaily1();
        mainVCalendar.addChild(vComponentOriginal);
        final List<VEvent> vComponents = mainVCalendar.getVEvents();
        VEvent vComponentEdited = new VEvent(vComponentOriginal);

        vComponentEdited.setSummary("Edited summary");

        Temporal startOriginalRecurrence = LocalDateTime.of(2016, 5, 16, 10, 0);
        Temporal startRecurrence = LocalDate.of(2016, 5, 16);
        Temporal endRecurrence = LocalDate.of(2016, 5, 17);

        ReviserVEvent reviser = ((ReviserVEvent) SimpleRevisorFactory.newReviser(vComponentOriginal))
                .withDialogCallback((m) -> ChangeDialogOption.ONE)
                .withEndRecurrence(endRecurrence)
                .withStartOriginalRecurrence(startOriginalRecurrence)
                .withStartRecurrence(startRecurrence)
                .withVComponentCopyEdited(vComponentEdited)
                .withVComponentOriginal(vComponentOriginal);
        List<VCalendar> iTIPMessages = reviser.revise();
        
        iTIPMessages.forEach(inputVCalendar -> mainVCalendar.processITIPMessage(inputVCalendar));
        assertEquals(2, vComponents.size());
        Collections.sort(vComponents, VPrimary.DTSTART_COMPARATOR);
        VEvent individualComponent = vComponents.get(1);
        
        String expectediTIPMessage =
                "BEGIN:VCALENDAR" + System.lineSeparator() +
                "METHOD:PUBLISH" + System.lineSeparator() +
                "PRODID:" + ICalendarAgenda.DEFAULT_PRODUCT_IDENTIFIER + System.lineSeparator() +
                "VERSION:" + Version.DEFAULT_ICALENDAR_SPECIFICATION_VERSION + System.lineSeparator() +
                "BEGIN:VEVENT" + System.lineSeparator() +
                "CATEGORIES:group05" + System.lineSeparator() +
                "DTSTART;VALUE=DATE:20160516" + System.lineSeparator() +
                "DTEND;VALUE=DATE:20160517" + System.lineSeparator() +
                "DESCRIPTION:Daily1 Description" + System.lineSeparator() +
                "SUMMARY:Edited summary" + System.lineSeparator() +
                individualComponent.getDateTimeStamp().toString() + System.lineSeparator() +
                "UID:20150110T080000-004@jfxtras.org" + System.lineSeparator() +
                "ORGANIZER;CN=Papa Smurf:mailto:papa@smurf.org" + System.lineSeparator() +
                "RECURRENCE-ID:20160516T100000" + System.lineSeparator() +
                "SEQUENCE:1" + System.lineSeparator() +
                "END:VEVENT" + System.lineSeparator() +
                "END:VCALENDAR";
        String iTIPMessage = iTIPMessages.stream()
                .map(v -> v.toString())
                .collect(Collectors.joining(System.lineSeparator()));
        assertEquals(expectediTIPMessage, iTIPMessage);
        
        // check DTSTAMP
        String dtstamp = iTIPMessage.split(System.lineSeparator())[10];
        String expectedDTStamp = new DateTimeStamp(ZonedDateTime.now().withZoneSameInstant(ZoneId.of("Z"))).toString();
        assertEquals(expectedDTStamp.substring(0, 16), dtstamp.substring(0, 16)); // check date, month and time
    }
    
    @Test
    public void canChangeWholeDayToTimeBasedOne()
    {
        VCalendar mainVCalendar = new VCalendar();
        VEvent vComponentOriginal = ICalendarStaticComponents.getWholeDayDaily1();
        mainVCalendar.addChild(vComponentOriginal);
        final List<VEvent> vComponents = mainVCalendar.getVEvents();
        VEvent vComponentEdited = new VEvent(vComponentOriginal);

        vComponentEdited.setSummary("Edited summary");

        Temporal startOriginalRecurrence = LocalDate.of(2016, 5, 16);
        Temporal startRecurrence = LocalDateTime.of(2016, 5, 16, 10, 0);
        Temporal endRecurrence = LocalDateTime.of(2016, 5, 16, 11, 0);

        ReviserVEvent reviser = ((ReviserVEvent) SimpleRevisorFactory.newReviser(vComponentOriginal))
                .withDialogCallback((m) -> ChangeDialogOption.ONE)
                .withEndRecurrence(endRecurrence)
                .withStartOriginalRecurrence(startOriginalRecurrence)
                .withStartRecurrence(startRecurrence)
                .withVComponentCopyEdited(vComponentEdited)
                .withVComponentOriginal(vComponentOriginal);
        List<VCalendar> iTIPMessages = reviser.revise();
        
        iTIPMessages.forEach(inputVCalendar -> mainVCalendar.processITIPMessage(inputVCalendar));
        assertEquals(2, vComponents.size());
        Collections.sort(vComponents, VPrimary.DTSTART_COMPARATOR);
        VEvent individualComponent = vComponents.get(1);
        
        String expectediTIPMessage =
                "BEGIN:VCALENDAR" + System.lineSeparator() +
                "METHOD:PUBLISH" + System.lineSeparator() +
                "PRODID:" + ICalendarAgenda.DEFAULT_PRODUCT_IDENTIFIER + System.lineSeparator() +
                "VERSION:" + Version.DEFAULT_ICALENDAR_SPECIFICATION_VERSION + System.lineSeparator() +
                "BEGIN:VEVENT" + System.lineSeparator() +
                "CATEGORIES:group06" + System.lineSeparator() +
                individualComponent.getDateTimeStamp().toString() + System.lineSeparator() +
                "UID:20150110T080000-010@jfxtras.org" + System.lineSeparator() +
                "DTSTART:20160516T100000" + System.lineSeparator() +
                "ORGANIZER;CN=Issac Newton:mailto:isaac@greatscientists.org" + System.lineSeparator() +
                "DTEND:20160516T110000" + System.lineSeparator() +
                "SUMMARY:Edited summary" + System.lineSeparator() +
                "RECURRENCE-ID;VALUE=DATE:20160516" + System.lineSeparator() +
                "SEQUENCE:1" + System.lineSeparator() +
                "END:VEVENT" + System.lineSeparator() +
                "END:VCALENDAR";
        String iTIPMessage = iTIPMessages.stream()
                .map(v -> v.toString())
                .collect(Collectors.joining(System.lineSeparator()));
        assertEquals(expectediTIPMessage, iTIPMessage);
        
        // check DTSTAMP
        String dtstamp = iTIPMessage.split(System.lineSeparator())[6];
        String expectedDTStamp = new DateTimeStamp(ZonedDateTime.now().withZoneSameInstant(ZoneId.of("Z"))).toString();
        assertEquals(expectedDTStamp.substring(0, 16), dtstamp.substring(0, 16)); // check date, month and time
    }
    
    @Test
    public void canChangeWholeDayToTimeBasedOne2()
    {
        VCalendar mainVCalendar = new VCalendar();
        LocalDate startDate = LocalDate.of(2016, 5, 16);
		VEvent vEventZonedInfinite = new VEvent()
                .withCategories(AgendaTestAbstract.DEFAULT_APPOINTMENT_GROUPS.get(3).getDescription())
                .withDateTimeEnd(ZonedDateTime.of(LocalDateTime.of(startDate.plusDays(1), LocalTime.of(12, 00)), ZoneId.of("America/Los_Angeles")))
                .withDateTimeStamp(ZonedDateTime.of(LocalDateTime.of(2015, 11, 10, 8, 0), ZoneOffset.UTC))
                .withDateTimeStart(ZonedDateTime.of(LocalDateTime.of(startDate.plusDays(1), LocalTime.of(7, 30)), ZoneId.of("America/Los_Angeles")))
                .withDescription("WeeklyZoned Description")
                .withRecurrenceRule(new RecurrenceRuleValue()
                        .withFrequency(FrequencyType.WEEKLY)
                        .withByRules(new ByDay(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)))
                .withSummary(Summary.parse("WeeklyZoned Infinite"))
                .withOrganizer("ORGANIZER;CN=Issac Newton:mailto:isaac@greatscientists.org")
                .withUniqueIdentifier("20150110T080000-02@jfxtras.org");
        mainVCalendar.addChild(vEventZonedInfinite);
        final List<VEvent> vComponents = mainVCalendar.getVEvents();
        VEvent vComponentEdited = new VEvent(vEventZonedInfinite);

        vComponentEdited.setSummary("Edited summary");

        Temporal startOriginalRecurrence = LocalDate.of(2016, 5, 16);
        Temporal startRecurrence = LocalDateTime.of(2016, 5, 16, 10, 0);
        Temporal endRecurrence = LocalDateTime.of(2016, 5, 16, 11, 0);

        ReviserVEvent reviser = ((ReviserVEvent) SimpleRevisorFactory.newReviser(vEventZonedInfinite))
                .withDialogCallback((m) -> ChangeDialogOption.ONE)
                .withEndRecurrence(endRecurrence)
                .withStartOriginalRecurrence(startOriginalRecurrence)
                .withStartRecurrence(startRecurrence)
                .withVComponentCopyEdited(vComponentEdited)
                .withVComponentOriginal(vEventZonedInfinite);
        List<VCalendar> iTIPMessages = reviser.revise();
        
        iTIPMessages.forEach(inputVCalendar -> mainVCalendar.processITIPMessage(inputVCalendar));
        assertEquals(2, vComponents.size());
        Collections.sort(vComponents, VPrimary.DTSTART_COMPARATOR);
        VEvent individualComponent = vComponents.get(1);
        
        String expectediTIPMessage =
                "BEGIN:VCALENDAR" + System.lineSeparator() +
                "METHOD:PUBLISH" + System.lineSeparator() +
                "PRODID:" + ICalendarAgenda.DEFAULT_PRODUCT_IDENTIFIER + System.lineSeparator() +
                "VERSION:" + Version.DEFAULT_ICALENDAR_SPECIFICATION_VERSION + System.lineSeparator() +
                "BEGIN:VEVENT" + System.lineSeparator() +
                "CATEGORIES:group06" + System.lineSeparator() +
                individualComponent.getDateTimeStamp().toString() + System.lineSeparator() +
                "UID:20150110T080000-010@jfxtras.org" + System.lineSeparator() +
                "DTSTART:20160516T100000" + System.lineSeparator() +
                "ORGANIZER;CN=Issac Newton:mailto:isaac@greatscientists.org" + System.lineSeparator() +
                "DTEND:20160516T110000" + System.lineSeparator() +
                "SUMMARY:Edited summary" + System.lineSeparator() +
                "RECURRENCE-ID;VALUE=DATE:20160516" + System.lineSeparator() +
                "SEQUENCE:1" + System.lineSeparator() +
                "END:VEVENT" + System.lineSeparator() +
                "END:VCALENDAR";
        String iTIPMessage = iTIPMessages.stream()
                .map(v -> v.toString())
                .collect(Collectors.joining(System.lineSeparator()));
        System.out.println(iTIPMessage);
        assertEquals(expectediTIPMessage, iTIPMessage);
        
        // check DTSTAMP
        String dtstamp = iTIPMessage.split(System.lineSeparator())[6];
        String expectedDTStamp = new DateTimeStamp(ZonedDateTime.now().withZoneSameInstant(ZoneId.of("Z"))).toString();
        assertEquals(expectedDTStamp.substring(0, 16), dtstamp.substring(0, 16)); // check date, month and time
    }
}

package com.saftyhub.project1.controller;

import com.saftyhub.project1.dto.SafetyEventDto;
import com.saftyhub.project1.model.SafetyEvent;
import com.saftyhub.project1.services.SafetyEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class EventsController {

    private final SafetyEventService eventService;

    @GetMapping("/events")
    public String eventsPage(
            @RequestParam(defaultValue = "ALL") String filter,
            @RequestParam(defaultValue = "en") String lang,
            @RequestParam(defaultValue="dark") String theme,
            Model model) {

        List<SafetyEventDto> events = filter.equalsIgnoreCase("TRAINING")
                ? eventService.getByType(SafetyEvent.EventType.TRAINING)
                : filter.equalsIgnoreCase("WARNING")
                ? eventService.getByType(SafetyEvent.EventType.WARNING)
                : eventService.getAll();

        model.addAttribute("events",        events);
        model.addAttribute("filter",        filter);
        model.addAttribute("totalEvents",   eventService.countAll());
        model.addAttribute("totalTraining", eventService.countTraining());
        model.addAttribute("totalWarnings", eventService.countWarnings());
        model.addAttribute("totalCritical", eventService.countCritical());
        model.addAttribute("lang",   lang);
        model.addAttribute("theme",  theme);
        model.addAttribute("activePage", "events");
        return "pages/events";
    }

    @PostMapping("/events/create")
    public String createEvent(
            @RequestParam String eventType,
            @RequestParam String title,
            @RequestParam(defaultValue = "") String description,
            @RequestParam(defaultValue = "") String eventDate,
            @RequestParam(defaultValue = "") String targetDepartment,
            @RequestParam(defaultValue = "") String warningMessage,
            @RequestParam(defaultValue = "2") Integer severity,
            @RequestParam(defaultValue = "en") String lang,
            @RequestParam(defaultValue="dark") String theme,
            RedirectAttributes ra) {
        try {
            eventService.create(eventType, title, description, eventDate, targetDepartment, warningMessage, severity);
            ra.addFlashAttribute("toast", lang.equals("ar") ? "تم نشر الفعالية بنجاح 🚀" : "Event published successfully 🚀");
        } catch (Exception e) {
            ra.addFlashAttribute("toast", lang.equals("ar") ? "حدث خطأ أثناء النشر" : "Error publishing event");
        }
        return "redirect:/events?lang=" + lang + "&theme=" + theme;
    }

    @PostMapping("/events/{id}/update")
    public String updateEvent(
            @PathVariable Long id,
            @RequestParam String eventType,
            @RequestParam String title,
            @RequestParam(defaultValue = "") String description,
            @RequestParam(defaultValue = "") String eventDate,
            @RequestParam(defaultValue = "") String targetDepartment,
            @RequestParam(defaultValue = "") String warningMessage,
            @RequestParam(defaultValue = "2") Integer severity,
            @RequestParam(defaultValue = "en") String lang,
            @RequestParam(defaultValue="dark") String theme,
            RedirectAttributes ra) {
        try {
            eventService.update(id, eventType, title, description, eventDate, targetDepartment, warningMessage, severity);
            ra.addFlashAttribute("toast", lang.equals("ar") ? "تم تحديث الفعالية" : "Event updated");
        } catch (Exception e) {
            ra.addFlashAttribute("toast", lang.equals("ar") ? "حدث خطأ أثناء التحديث" : "Error updating event");
        }
        return "redirect:/events?lang=" + lang + "&theme=" + theme;
    }

    @PostMapping("/events/{id}/delete")
    public String deleteEvent(
            @PathVariable Long id,
            @RequestParam(defaultValue = "en") String lang,
            @RequestParam(defaultValue="dark") String theme,
            RedirectAttributes ra) {
        try {
            eventService.delete(id);
            ra.addFlashAttribute("toast", lang.equals("ar") ? "تم حذف الفعالية" : "Event deleted");
        } catch (Exception e) {
            ra.addFlashAttribute("toast", lang.equals("ar") ? "حدث خطأ أثناء الحذف" : "Error deleting event");
        }
        return "redirect:/events?lang=" + lang + "&theme=" + theme;
    }

    // ── REST endpoint for notification polling ──────────────────
    @GetMapping("/api/events/latest")
    @ResponseBody
    public List<SafetyEventDto> getLatestEvents(
            @RequestParam(defaultValue = "10") int limit,
            jakarta.servlet.http.HttpSession session) {
        // Only return events to authenticated users
        if (session.getAttribute("accountId") == null) {
            return java.util.Collections.emptyList();
        }
        return eventService.getAll().stream()
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
    }

}

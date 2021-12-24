package vacationservice;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class VacationCalculateServiceController {

    @RequestMapping("/vacation")
    public String vacation(@RequestParam(name="jobband") String jobband,
                           @RequestParam(name="serviceyears") int serviceyears,
                           Model model) {
        int vacationDays=0;
        vacationDays=VacationBusinessLogic.getVacationDays(jobband,serviceyears);
        model.addAttribute("jobband", jobband);
        model.addAttribute("serviceyears", serviceyears);
        model.addAttribute("vacationdays", vacationDays);
        return "result";
    }
}

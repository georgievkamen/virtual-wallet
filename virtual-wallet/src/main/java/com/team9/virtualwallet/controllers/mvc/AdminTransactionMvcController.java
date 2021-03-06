package com.team9.virtualwallet.controllers.mvc;

import com.team9.virtualwallet.controllers.AuthenticationHelper;
import com.team9.virtualwallet.exceptions.AuthenticationFailureException;
import com.team9.virtualwallet.exceptions.EntityNotFoundException;
import com.team9.virtualwallet.exceptions.UnauthorizedOperationException;
import com.team9.virtualwallet.models.Pages;
import com.team9.virtualwallet.models.Transaction;
import com.team9.virtualwallet.models.User;
import com.team9.virtualwallet.models.enums.Direction;
import com.team9.virtualwallet.models.enums.Sort;
import com.team9.virtualwallet.services.contracts.TransactionService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/panel/admin/transactions")
public class AdminTransactionMvcController extends BaseAuthenticationController {

    private final AuthenticationHelper authenticationHelper;
    private final TransactionService service;

    public AdminTransactionMvcController(AuthenticationHelper authenticationHelper, TransactionService service) {
        super(authenticationHelper);
        this.authenticationHelper = authenticationHelper;
        this.service = service;
    }

    @ModelAttribute("direction")
    public List<Direction> populateDirection() {
        return Arrays.asList(Direction.values());
    }

    @ModelAttribute("sort")
    public List<Sort> populateSort() {
        return Arrays.asList(Sort.values());
    }

    @GetMapping
    public String showAdminTransactionsPage(HttpSession session, Model model,
                                            @RequestParam(name = "username", required = false) Optional<String> username,
                                            @RequestParam(name = "counterparty", required = false) Optional<String> counterparty,
                                            @RequestParam(name = "direction", required = false) Optional<String> direction,
                                            @RequestParam(name = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Optional<Date> startDate,
                                            @RequestParam(name = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Optional<Date> endDate,
                                            @RequestParam(name = "sortAmount", required = false) Optional<String> sortAmount,
                                            @RequestParam(name = "sortDate", required = false) Optional<String> sortDate,
                                            @PageableDefault(page = 1) Pageable pageable) {
        try {
            User user = authenticationHelper.tryGetUser(session);
            if (!user.isEmployee()) {
                return "redirect:/panel";
            }
            if (username.isPresent()) {
                if (username.get().isBlank()) {
                    model.addAttribute("error", "You must provide an username!");
                    return "transactions-admin";
                }
                Pages<Transaction> filtered = service.employeeFilter(user,
                        username.get(),
                        counterparty.isPresent() && counterparty.get().isBlank() ? Optional.empty() : counterparty,
                        direction.filter(s -> !s.equals("-1")).map(Direction::getEnum),
                        startDate,
                        endDate,
                        sortAmount.filter(s -> !s.equals("-1")).map(Sort::getEnum),
                        sortDate.filter(s -> !s.equals("-1")).map(Sort::getEnum),
                        pageable);
                model.addAttribute("transactions", filtered.getContent());
                model.addAttribute("transactionsExist", !filtered.getContent().isEmpty());
                model.addAttribute("pagination", filtered);
            }
            return "transactions-admin";
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        } catch (UnauthorizedOperationException e) {
            return "redirect:/panel";
        } catch (EntityNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            return "transactions-admin";
        }
    }

}
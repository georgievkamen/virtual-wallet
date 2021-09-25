package com.team9.virtualwallet.controllers.mvc;

import com.team9.virtualwallet.controllers.AuthenticationHelper;
import com.team9.virtualwallet.exceptions.AuthenticationFailureException;
import com.team9.virtualwallet.exceptions.EntityNotFoundException;
import com.team9.virtualwallet.models.Transaction;
import com.team9.virtualwallet.models.User;
import com.team9.virtualwallet.models.dtos.TransactionDto;
import com.team9.virtualwallet.services.contracts.CardService;
import com.team9.virtualwallet.services.contracts.TransactionService;
import com.team9.virtualwallet.services.contracts.UserService;
import com.team9.virtualwallet.services.contracts.WalletService;
import com.team9.virtualwallet.services.mappers.TransactionModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/panel/transactions")
public class TransactionMvcController {

    private final AuthenticationHelper authenticationHelper;
    private final TransactionService service;
    private final TransactionModelMapper modelMapper;
    private final CardService cardService;
    private final WalletService walletService;
    private final UserService userService;

    public TransactionMvcController(AuthenticationHelper authenticationHelper, TransactionService service, TransactionModelMapper modelMapper, CardService cardService, WalletService walletService, UserService userService) {
        this.authenticationHelper = authenticationHelper;
        this.service = service;
        this.modelMapper = modelMapper;
        this.cardService = cardService;
        this.walletService = walletService;
        this.userService = userService;
    }

    //TODO Think about moving this to BaseAuthenticationController
    @ModelAttribute("currentLoggedUser")
    public String populateCurrentLoggedUser(HttpSession session, Model model) {
        try {
            User user = authenticationHelper.tryGetUser(session);
            model.addAttribute("currentLoggedUser", user);
            return "";
        } catch (AuthenticationFailureException e) {
            return "/auth/login";
        }
    }

    @GetMapping
    public String showTransactionsPage(HttpSession session, Model model) {
        try {
            User user = authenticationHelper.tryGetUser(session);
            List<Transaction> transactions = service.getAll(user);
            model.addAttribute("transactions", transactions);
            model.addAttribute("transactionsExist", !transactions.isEmpty());
            model.addAttribute("cardService", cardService);
            model.addAttribute("walletService", walletService);
            return "transactions";
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/create")
    public String showInternalTransactionPage(HttpSession session, Model model, @RequestParam(name = "fieldName", required = false) String fieldName, @RequestParam(name = "search-field", required = false) String searchTerm) {
        try {
            User user = authenticationHelper.tryGetUser(session);
            model.addAttribute("transaction", new TransactionDto());
            model.addAttribute("userWallets", walletService.getAll(user));
            if (fieldName != null && searchTerm != null) {
                model.addAttribute("user", userService.getByField(user, fieldName, searchTerm));
            }
            return "transaction-internal-create";
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        } catch (EntityNotFoundException e) {
            model.addAttribute("notFound", "User not found!");
            return "transaction-internal-create";
        }
    }

    @PostMapping("/create")
    public String createInternalTransaction(@Valid @ModelAttribute("transaction") TransactionDto transactionDto, BindingResult result, HttpSession session, Model model, @RequestParam(required = false) Optional<Integer> categoryId) {
        try {
            User user = authenticationHelper.tryGetUser(session);
            model.addAttribute("userWallets", walletService.getAll(user));
            if (result.hasErrors()) {
                return "transaction-internal-create";
            }

            model.addAttribute("transaction", new TransactionDto());
            Transaction transaction = modelMapper.fromDto(user, transactionDto);
            service.create(transaction, categoryId);
            return "redirect:/panel/transactions";
        } catch (AuthenticationFailureException e) {
            return "redirect:/auth/login";
        } catch (IllegalArgumentException e) {
            result.rejectValue("amount", "insufficient_balance", e.getMessage());
            return "transaction-internal-create";
        } catch (EntityNotFoundException e) {
            result.rejectValue("recipientId", "not_found", e.getMessage());
            return "transaction-internal-create";
        }
    }

}
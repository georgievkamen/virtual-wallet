package com.team9.virtualwallet.controllers.rest;

import com.team9.virtualwallet.controllers.AuthenticationHelper;
import com.team9.virtualwallet.models.Transaction;
import com.team9.virtualwallet.models.User;
import com.team9.virtualwallet.models.dtos.ExternalTransactionDto;
import com.team9.virtualwallet.models.dtos.MoveToWalletTransactionDto;
import com.team9.virtualwallet.models.dtos.TransactionDto;
import com.team9.virtualwallet.models.enums.Direction;
import com.team9.virtualwallet.models.enums.SortAmount;
import com.team9.virtualwallet.models.enums.SortDate;
import com.team9.virtualwallet.services.contracts.TransactionService;
import com.team9.virtualwallet.services.mappers.TransactionModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.team9.virtualwallet.configs.RestResponseEntityExceptionHandler.checkFields;
import static com.team9.virtualwallet.utils.PagingHelper.getPage;

@RestController
@RequestMapping("/api/transactions")
public class TransactionRestController {

    private final TransactionService service;
    private final AuthenticationHelper authenticationHelper;
    private final TransactionModelMapper modelMapper;

    @Autowired
    public TransactionRestController(TransactionService service,
                                     AuthenticationHelper authenticationHelper, TransactionModelMapper modelMapper) {
        this.service = service;
        this.authenticationHelper = authenticationHelper;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public List<Transaction> getAll(@RequestHeader HttpHeaders headers,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "5") int size) {
        User user = authenticationHelper.tryGetUser(headers);

        return getPage(service.getAll(user), page, size);
    }

    @GetMapping("/{id}")
    public Transaction getById(@RequestHeader HttpHeaders headers, @PathVariable int id) {
        User user = authenticationHelper.tryGetUser(headers);

        return service.getById(user, id);
    }

    @PostMapping
    public Transaction create(@RequestHeader HttpHeaders headers, @RequestBody @Valid TransactionDto transactionDto, BindingResult result, @RequestParam(required = false) Optional<Integer> categoryId) {
        checkFields(result);

        User user = authenticationHelper.tryGetUser(headers);

        //TODO Do we need create a transaction by username//email//phone number
        Transaction transaction = modelMapper.fromDto(user, transactionDto);
        service.create(transaction, categoryId);

        return transaction;
    }

    @PostMapping("/internal/move")
    public Transaction moveToWallet(@RequestHeader HttpHeaders headers, @RequestBody @Valid MoveToWalletTransactionDto moveToWalletTransactionDto, BindingResult result, @RequestParam(required = false) Optional<Integer> categoryId) {
        checkFields(result);

        User user = authenticationHelper.tryGetUser(headers);

        Transaction transaction = modelMapper.fromDtoMoveToWallet(user, moveToWalletTransactionDto);
        service.createWalletToWallet(transaction);

        return transaction;
    }

    @PostMapping("/external/deposit")
    public Transaction createExternalDeposit(@RequestHeader HttpHeaders headers, @RequestBody @Valid ExternalTransactionDto externalTransactionDto, BindingResult result, @RequestParam(required = false) Optional<Integer> categoryId) {
        checkFields(result);

        User user = authenticationHelper.tryGetUser(headers);

        RestTemplate restTemplate = new RestTemplate();
        HttpStatus status = restTemplate.getForObject("http://localhost/api/dummy", HttpStatus.class);
        if (status != null && status.isError()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Sorry your transfer is rejected");
        }

        Transaction transaction = modelMapper.fromExternalDepositDto(user, externalTransactionDto);
        service.createExternalDeposit(transaction);

        return transaction;
    }

    @PostMapping("/external/withdraw")
    public Transaction createExternalWithdraw(@RequestHeader HttpHeaders headers, @RequestBody @Valid ExternalTransactionDto externalTransactionDto, BindingResult result, @RequestParam(required = false) Optional<Integer> categoryId) {
        checkFields(result);

        User user = authenticationHelper.tryGetUser(headers);

        RestTemplate restTemplate = new RestTemplate();
        HttpStatus status = restTemplate.getForObject("http://localhost/api/dummy", HttpStatus.class);
        if (status != null && status.isError()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Sorry your transfer is rejected");
        }

        Transaction transaction = modelMapper.fromExternalWithdrawDto(user, externalTransactionDto);
        service.createExternalWithdraw(transaction);

        return transaction;
    }

    @GetMapping("/filter")
    public List<Transaction> filter(@RequestHeader HttpHeaders headers,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "5") int size,
                                    @RequestParam(required = false)
                                                Optional<Date> startDate,
                                    Optional<Date> endDate,
                                    Optional<Integer> categoryId,
                                    Optional<Integer> senderId,
                                    Optional<Integer> recipientId,
                                    Optional<Direction> direction,
                                    Optional<SortAmount> amount,
                                    Optional<SortDate> date) {

        User user = authenticationHelper.tryGetUser(headers);

        return getPage(service.filter(user, startDate, endDate, categoryId, senderId, recipientId, direction, amount, date), page, size);
    }

}



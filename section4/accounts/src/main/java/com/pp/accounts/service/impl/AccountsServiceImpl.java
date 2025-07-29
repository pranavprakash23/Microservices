package com.pp.accounts.service.impl;

import com.pp.accounts.constants.AccountsConstants;
import com.pp.accounts.dto.AccountsDto;
import com.pp.accounts.dto.CustomerDto;
import com.pp.accounts.entity.Accounts;
import com.pp.accounts.entity.Customer;
import com.pp.accounts.exception.CustomerAlreadyExistsException;
import com.pp.accounts.exception.ResourceNotFoundException;
import com.pp.accounts.mapper.AccountsMapper;
import com.pp.accounts.mapper.CustomerMapper;
import com.pp.accounts.repository.AccountsRepository;
import com.pp.accounts.repository.CustomerRepository;
import com.pp.accounts.service.IAccountsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AccountsServiceImpl implements IAccountsService {

    @Autowired
    private AccountsRepository accountsRepository;
    @Autowired
    private CustomerRepository customerRepository;

    public AccountsServiceImpl() {
    }

    public AccountsServiceImpl(AccountsRepository accountsRepository, CustomerRepository customerRepository) {
        this.accountsRepository = accountsRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public void createAccount(CustomerDto customerDto) {
        Customer customer = CustomerMapper.mapToCustomer(customerDto, new Customer());

        Optional<Customer> customerData = customerRepository.findByMobileNumber(customerDto.getMobileNumber());
        if(customerData.isPresent()){
            throw new CustomerAlreadyExistsException("Customer already registered with the given mobile number "+ customerDto.getMobileNumber());
        }
        Customer savedCustomer = customerRepository.save(customer);
        Accounts accounts = createNewAccount(savedCustomer);
        accountsRepository.save(accounts);
    }

    private Accounts createNewAccount(Customer customer) {
        Accounts newAccount = new Accounts();
        newAccount.setCustomerId(customer.getCustomerId());
        long randomAccNumber = 1000000000L + new Random().nextInt(900000000);

        newAccount.setAccountNumber(randomAccNumber);
        newAccount.setAccountType(AccountsConstants.SAVINGS);
        newAccount.setBranchAddress(AccountsConstants.ADDRESS);
        return newAccount;
    }

    @Override
    public CustomerDto fetchAccountDetails(String mobileNumber) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(()->new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber));

        Accounts account = accountsRepository.findByCustomerId(customer.getCustomerId())
                .orElseThrow(()->new ResourceNotFoundException("Accounts", "customerId", customer.getCustomerId().toString()));

        CustomerDto customerDto = CustomerMapper.mapToCustomerDto(customer,new CustomerDto());
        customerDto.setAccountsDto(AccountsMapper.mapToAccountsDto(account, new AccountsDto()));
        return customerDto;
    }

    @Override
    public boolean updateAccount(CustomerDto customerDto) {

        boolean isUpdate = false;
        AccountsDto accountsDto = customerDto.getAccountsDto();
        if(accountsDto!=null){

            Accounts accounts = accountsRepository.findById(accountsDto.getAccountNumber()).orElseThrow(
                    ()-> new ResourceNotFoundException("Accounts", "Account Number", accountsDto.getAccountNumber().toString())
            );
            AccountsMapper.mapToAccounts(customerDto.getAccountsDto(), accounts);
            accounts = accountsRepository.save(accounts);

            Long customerId = accounts.getCustomerId();

            Customer customer = customerRepository.findById(customerId).orElseThrow(
                    ()-> new ResourceNotFoundException("Customer", "Customer Id", customerId.toString())
            );
                    CustomerMapper.mapToCustomer(customerDto, new Customer());
            customerRepository.save(customer);
            isUpdate = true;

        }

        return isUpdate;

    }

    @Override
    public boolean deleteAccount(String mobileNumber) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
        );
        accountsRepository.deleteByCustomerId(customer.getCustomerId());
        customerRepository.deleteById(customer.getCustomerId());
        return true;
    }


}

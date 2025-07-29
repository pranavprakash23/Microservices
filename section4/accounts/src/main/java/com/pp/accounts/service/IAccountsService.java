package com.pp.accounts.service;

import com.pp.accounts.dto.CustomerDto;

public interface IAccountsService {

    public void createAccount(CustomerDto customerDto);

    public CustomerDto fetchAccountDetails(String mobileNumber);

    public boolean updateAccount(CustomerDto customerDto);

    public boolean deleteAccount(String mobileNumber);
}

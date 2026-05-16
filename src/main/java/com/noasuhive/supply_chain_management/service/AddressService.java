package com.noasuhive.supply_chain_management.service;

import com.noasuhive.supply_chain_management.models.Address;

import java.util.List;
import java.util.UUID;

public interface AddressService {
    Address createAddress(Address address, UUID userId);
    List<Address> getUserAddresses(UUID userId);
    Address getAddressById(UUID addressId, UUID userId);
    Address updateAddress(UUID addressId, UUID userId, Address address);
    void deleteAddress(UUID addressId, UUID userId);
}

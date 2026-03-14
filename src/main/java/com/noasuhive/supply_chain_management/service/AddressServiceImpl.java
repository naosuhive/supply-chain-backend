package com.noasuhive.supply_chain_management.service;

import com.noasuhive.supply_chain_management.exceptions.UnauthorizedAccessException;
import com.noasuhive.supply_chain_management.models.Address;
import com.noasuhive.supply_chain_management.repositories.AddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    public AddressServiceImpl(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    @Transactional
    public Address createAddress(Address address, UUID userId) {
        address.setUserId(userId);
        return addressRepository.save(address);
    }

    @Override
    public List<Address> getUserAddresses(UUID userId) {
        return addressRepository.findByUserId(userId);
    }

    @Override
    public Address getAddressById(UUID addressId, UUID userId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));
        
        // Verify the address belongs to the user
        if (!address.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("You can only access your own addresses");
        }
        
        return address;
    }

    @Override
    @Transactional
    public Address updateAddress(UUID addressId, UUID userId, Address address) {
        Address existingAddress = getAddressById(addressId, userId);
        
        // Update fields
        existingAddress.setLine1(address.getLine1());
        existingAddress.setLine2(address.getLine2());
        existingAddress.setCity(address.getCity());
        existingAddress.setState(address.getState());
        existingAddress.setCountry(address.getCountry());
        existingAddress.setPostalCode(address.getPostalCode());
        existingAddress.setAddressType(address.getAddressType());
        existingAddress.setPrimary(address.isPrimary());
        
        return addressRepository.save(existingAddress);
    }

    @Override
    @Transactional
    public void deleteAddress(UUID addressId, UUID userId) {
        Address address = getAddressById(addressId, userId);
        addressRepository.delete(address);
    }
}

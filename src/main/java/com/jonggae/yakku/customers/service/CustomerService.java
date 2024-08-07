package com.jonggae.yakku.customers.service;

import com.jonggae.yakku.address.Address;
import com.jonggae.yakku.common.exceptions.DuplicateMemberException;
import com.jonggae.yakku.common.exceptions.NotFoundMemberException;
import com.jonggae.yakku.common.redis.TokenService;
import com.jonggae.yakku.customers.dto.CustomerMyPageResponseDto;
import com.jonggae.yakku.customers.dto.CustomerRequestDto;
import com.jonggae.yakku.customers.dto.CustomerResponseDto;
import com.jonggae.yakku.customers.dto.CustomerUpdateDto;
import com.jonggae.yakku.customers.entity.Authority;
import com.jonggae.yakku.customers.entity.Customer;
import com.jonggae.yakku.customers.entity.UserRoleEnum;
import com.jonggae.yakku.customers.repository.AuthorityRepository;
import com.jonggae.yakku.customers.repository.CustomerRepository;
import com.jonggae.yakku.mailVerification.service.MailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final TokenService tokenService;
    private final AuthorityRepository authorityRepository;


    public void register(CustomerRequestDto customerRequestDto) {
        logger.debug("Registering customer: {}", customerRequestDto);

        checkCustomerInfo(customerRequestDto.getCustomerName(), customerRequestDto.getEmail());

        String token = tokenService.createEmailToken(customerRequestDto.getEmail(), customerRequestDto);
        mailService.sendMail(customerRequestDto.getEmail(), token);
    }

    public CustomerResponseDto confirmCustomer(String token) {
        CustomerRequestDto customerRequestDto = tokenService.getUserDetailsByEmailToken(token);
        logger.debug("Retrieved CustomerResponseDto from token: {}", customerRequestDto);

        if (customerRequestDto != null) {

            Authority authority = Authority.builder()
                    .authorityName(UserRoleEnum.ROLE_USER).build();
            authorityRepository.save(authority);

            Address address = Address.builder()
                    .address(customerRequestDto.getAddress())
                    .addressDetail(customerRequestDto.getAddressDetail())
                    .build();

            Customer customer = Customer.builder()
                    .customerName(customerRequestDto.getCustomerName())
                    .password(passwordEncoder.encode(customerRequestDto.getPassword()))
                    .email(customerRequestDto.getEmail())
                    .address(address)
                    .authorities(Collections.singleton(authority))
                    .enabled(true)
                    .build();

            customerRepository.save(customer);
            tokenService.deleteEmailToken(token);
            return CustomerResponseDto.from(customer);

        } else {
            throw new IllegalStateException("유효하지 않거나 만료된 인증 확인입니다.");
        }
    }

    //todo: 액세스 토큰이 만료되었을 때도 아래 오류가 뜸. 다른 예외처리로 수정필요
    //todo : 마이페이지 내에서 주문상품과 위시리스트도 보여주어야함

    public CustomerMyPageResponseDto getMyPage(String customerName) {
        Customer customer = customerRepository.findOneWithAuthoritiesByCustomerName(customerName)
                .orElseThrow(NotFoundMemberException::new);
        return CustomerMyPageResponseDto.from(customer);
    }

    @Transactional
    public CustomerMyPageResponseDto updateMyPage(String customerName, CustomerUpdateDto updateDto) {
        Customer customer = customerRepository.findByCustomerName(customerName)
                .orElseThrow(NotFoundMemberException::new);
        customer.updateCustomerInfo(updateDto, passwordEncoder);
        customerRepository.save(customer);
        return CustomerMyPageResponseDto.from(customer);

    }

    private void checkCustomerInfo(String customerName, String email) {
        if (customerRepository.findByCustomerName(customerName).isPresent()) {
            throw new DuplicateMemberException("이미 가입된 이름입니다.");
        }
        if (customerRepository.findByEmail(email).isPresent()) {
            throw new DuplicateMemberException("이미 사용중인 이메일 주소 입니다.");
        }
    }

}

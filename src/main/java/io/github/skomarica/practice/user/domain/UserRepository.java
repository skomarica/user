package io.github.skomarica.practice.user.domain;

import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author Sinisa Komarica
 */
interface UserRepository extends PagingAndSortingRepository<User, Long> {
}

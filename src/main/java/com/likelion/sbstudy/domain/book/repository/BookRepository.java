package com.likelion.sbstudy.domain.book.repository;

import com.likelion.sbstudy.domain.book.entity.Book;
import com.likelion.sbstudy.domain.book.entity.Category;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

  Optional<Book> findByTitleAndAuthor(String title, String author);

  Page<Book> findAllByCategoryListContaining(Category category, Pageable pageable);

  Page<Book> findAllByCategoryListContainingAndIdLessThan(Category category, Long lastBookId, Pageable pageable);
}

 package com.thoughtworks.lirenlab.interfaces.donation.facade.internal.assembler;
 
 import com.google.common.base.Function;
 import com.thoughtworks.lirenlab.domain.model.donation.Book;
 import com.thoughtworks.lirenlab.domain.model.donation.Donation;
 import com.thoughtworks.lirenlab.interfaces.donation.facade.dto.BookDTO;
 import com.thoughtworks.lirenlab.interfaces.donation.facade.dto.DonationDTO;
 
 import java.util.List;
 
 import static com.google.common.collect.Lists.transform;
 import static com.thoughtworks.lirenlab.domain.model.donation.Book.newBook;
 
 public class DonationDTOAssembler {
 
     public List<DonationDTO> toDonationDTOs(List<Donation> donations) {
         return transform(donations, toDonationDTOFunction());
     }
 
     public DonationDTO toDonationDTO(Donation donation) {
         return toDonationDTOFunction().apply(donation);
     }
 
     public List<Book> fromBookDTOs(List<BookDTO> bookDTOs) {
         return transform(bookDTOs, fromBookDTOFunction());
     }
 
     private Function<BookDTO, Book> fromBookDTOFunction() {
         return new Function<BookDTO, Book>() {
             @Override
             public Book apply(BookDTO input) {
                 return newBook(input.getIsbn(), input.getTitle());
             }
         };
     }
 
 
     private Function<Donation, DonationDTO> toDonationDTOFunction() {
         return new Function<Donation, DonationDTO>() {
             @Override
             public DonationDTO apply(Donation donation) {
                 DonationDTO donationDTO = new DonationDTO();
                 donationDTO.setId(donation.id().strValue());
                 donationDTO.setStatus(donation.status().toString());
                donationDTO.setCreatedTimeStamp(unixTimeStamp(donation));
                 donationDTO.setBookAmount(donation.books().size());
                 donationDTO.setBooks(transform(donation.books(), toBookDTOFunction()));
                 return donationDTO;
             }

            private long unixTimeStamp(Donation donation) {
                return donation.createdDate().getTime() / 1000L;
            }
         };
     }
 
     private Function<Book, BookDTO> toBookDTOFunction() {
         return new Function<Book, BookDTO>() {
             @Override
             public BookDTO apply(Book book) {
                 BookDTO dto = new BookDTO();
                 dto.setIsbn(book.isbn());
                 dto.setStatus(book.status().toString());
                 dto.setTitle(book.title());
                 return dto;
             }
         };
     }
 }

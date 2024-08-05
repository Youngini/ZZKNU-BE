package com.zzknu.back_end.domain.quote.service;

import com.zzknu.back_end.domain.jwt.JwtService;
import com.zzknu.back_end.domain.quote.dto.QuoteRequestDto;
import com.zzknu.back_end.domain.quote.dto.QuoteResponse;
import com.zzknu.back_end.domain.quote.dto.QuoteUpdateRequestDto;
import com.zzknu.back_end.domain.quote.dto.ResponseSuccessful;
import com.zzknu.back_end.domain.quote.entity.Quote;
import com.zzknu.back_end.domain.quote.repository.QuoteRepository;
import com.zzknu.back_end.domain.user.entity.User;
import com.zzknu.back_end.domain.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuoteService {
    private final QuoteRepository quoteRepository;
    private final JwtService jwtService;
    private final UserService userService;

    public Quote findById(Long quoteId) {
        return quoteRepository.findById(quoteId)
                .orElseThrow(() -> new RuntimeException("Quote not found"));
    }
    // 글귀 등록
    @Transactional
    public ResponseSuccessful createQuote(String accessToken, QuoteRequestDto quoteRequestDto) {
        String email = jwtService.getEmailFromToken(accessToken);
        User existingUser = userService.findByEmail(email);
        if (existingUser == null) {
            throw new RuntimeException("User not found");
        }
        quoteRepository.save(Quote.toEntity(existingUser, quoteRequestDto));
        return ResponseSuccessful.builder().success(true).build();
    }

    // 글귀 수정
    @Transactional
    public ResponseSuccessful updateQuote(String accessToken, QuoteUpdateRequestDto quoteUpdateRequestDto) {
        // accessToken 주인
        String email = jwtService.getEmailFromToken(accessToken);
        User existingUser = userService.findByEmail(email);
        if (existingUser == null) {
            throw new RuntimeException("User not found");
        }
        // 글귀 주인
        Quote quote = quoteRepository.findById(quoteUpdateRequestDto.getId()).orElse(null);
        if (quote == null) {
            throw new RuntimeException("Quote not found");
        }
        // 글귀의 주인인지 판단
        if (existingUser != quote.getUser()) {
            throw new RuntimeException("No permission to update quote");
        }
        else {
            quote.update(quoteUpdateRequestDto);
            return ResponseSuccessful.builder().success(true).build();
        }
    }

    // 모든 글귀 열람 (WAIT)
    public Page<QuoteResponse> getWaitQuotes(Pageable pageable){
        Page <Quote> quotes = quoteRepository.findByCertified(false, pageable);
        return quotes.map(QuoteResponse::new);
    }

    // 모든 글귀 열람 (인증된)
    public Page<QuoteResponse> getAcceptedQuotes(Pageable pageable){
        Page <Quote> quotes = quoteRepository.findByCertified(true, pageable);
        return quotes.map(QuoteResponse::new);
    }

    // id로 글귀 1개 찾기 - 특정 글귀 열람, 글귀 좋아요에도 쓰일 듯
    public QuoteResponse getQuoteById(Long id) {
       Quote quote = quoteRepository.findById(id).orElse(null);
       if (quote == null) {
           throw new RuntimeException("Quote not found");
       }
       return new QuoteResponse(quote);
    }

    // 글귀 삭제
    public void deleteQuoteById(Long id) {
        quoteRepository.deleteById(id);
    }

    // 좋아요 반영
    @Transactional
    public void increaseLikes(Long id){
        Quote quote = quoteRepository.findById(id).orElse(null);
        if (quote != null) {
            quote.increaseLikes();
        }
        // quoteRepository.findById(id).ifPresent(Quote::increaseLikes); -> 이렇게 한 줄로 줄여짐;;
    }

    // 작성자로 검색
    public Page<QuoteResponse> getQuotesByAuthor(String author, Pageable pageable) {
        Page<Quote> quotes = quoteRepository.findByAuthor(author, pageable);
        return quotes.map(QuoteResponse::new);
    }

    public boolean updateQuoteCertify(Long quoteId, Boolean certified) {
        Quote quote = quoteRepository.findById(quoteId).orElse(null);
        if (quote == null) {
            throw new RuntimeException("Quote not found");
        }
        quote.setCertified(certified);
        quoteRepository.save(quote);
        return true;
    }

    // 좋아요 10개 이상인 게시글
    public Page<Quote> getChallengeQuote(Pageable pageable) {
        List<Quote> challengeQuotes = quoteRepository.findByCertified(false, pageable).getContent();
        List<Quote> filteredQuotes = challengeQuotes.stream()
                .filter(quote -> quote.getLikedQuotes().size() >= 10)
                .toList();
        // Pageable을 사용하여 결과를 페이지로 나누기
        int start = Math.toIntExact(pageable.getOffset());
        int end = Math.min((start + pageable.getPageSize()), filteredQuotes.size());
        List<Quote> pagedQuotes = filteredQuotes.subList(start, end);

        return new PageImpl<>(pagedQuotes, pageable, filteredQuotes.size());
    }

    // 제목 검색 기능 and 카테고리별 정렬 기능 미구현
}

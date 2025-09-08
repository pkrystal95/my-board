package com.example.my_board.filter;

import com.example.my_board.model.entity.RefreshToken;
import com.example.my_board.model.repository.RefreshTokenRepository;
import com.example.my_board.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class RefreshJwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String accessToken = com.example.my_board.util.CookieUtil.findCookie(request, "access_token");
        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }
        // Refresh Token의 경우...
        try {
            // Access Token을 검증해서... (DB랑 비교해서 쿼리를 날려본게 X)
            jwtUtil.validateToken(accessToken);
        } catch (ExpiredJwtException ex) {
            // 만료 시에는 알아서 재발급
            handleRefreshToken(request, response);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }


    private void handleRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 1. RefreshToken CookieUtil -> Request
            String refreshToken = com.example.my_board.util.CookieUtil.findCookie(request, "refresh_token");
            if (refreshToken == null) {
                return;
            }
            String username = jwtUtil.getUsername(refreshToken); // refresh -> username
            // 2. repository -> 저장되었는지 비교 -> 검증
            RefreshToken stored = refreshTokenRepository.findById(username)
                    .orElseThrow(() -> new RuntimeException("Redis에 Refresh 없음")); // stored -> store.
            if (!refreshToken.equals(stored.getToken())) {
                throw new RuntimeException("Refresh Token 불일치");
            }
            // 3. accessToken 재발급 -> cookie.
            String role = jwtUtil.getRole(refreshToken);
            String newAccessToken = jwtUtil.generateToken(username, role, false);
            com.example.my_board.util.CookieUtil.createCookie(response, "access_token", newAccessToken, 60 * 60);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            // 이슈가 생기면... 내부에서 try-catch 예외 처리
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
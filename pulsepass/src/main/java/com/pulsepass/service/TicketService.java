package com.pulsepass.service;

import java.util.List;

import com.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.dto.response.TicketResponse;

public interface TicketService {
    TicketResponse purchase(PurchaseTicketRequest request);
    TicketResponse findByCode(String ticketCode);
    List<TicketResponse> findByUserEmail(String email);
    List<TicketResponse> findPaidTicketsByEvent(String eventCode);
    TicketResponse cancel (String ticketCode);
    TicketResponse markAsUsed (String ticketCode);

    
}

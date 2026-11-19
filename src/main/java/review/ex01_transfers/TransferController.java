package review.ex01_transfers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<Object> transfer(@RequestBody TransferRequest request) {
        TransferReceipt receipt = transferService.transfer(request);
        if (receipt == null) {
            return ResponseEntity.ok("Transfer failed");
        }
        return ResponseEntity.ok(receipt);
    }
}

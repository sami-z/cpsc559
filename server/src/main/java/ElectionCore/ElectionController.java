package ElectionCore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
public class ElectionController{

    @Autowired
    private ElectionState state;

    @PostMapping("/leader")
    public void leader(@RequestBody ElectionState es) {
        state.leaderIP = es.leaderIP;
        state.isRunning = es.isRunning;
    }

    @PostMapping("/election")
    @ResponseStatus(value = HttpStatus.OK)
    public void election(@RequestBody ElectionState es) throws InterruptedException {

    }


}

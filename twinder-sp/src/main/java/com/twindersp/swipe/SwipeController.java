package com.twindersp.swipe;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/swipe")
@RestController
public class SwipeController {

  @GetMapping("/")
  public ResponseEntity<String> swipe() {
    return new ResponseEntity<String>("swipe successful", HttpStatus.OK);
  }

  @PostMapping("/{direction}")
  public ResponseEntity<String> swipe(@PathVariable("direction") @NotBlank String direction,
      @Valid @RequestBody Swipe swipe, Errors errors) {
    if (errors.hasErrors()) {
      return new ResponseEntity<String>("invalid inputs", HttpStatus.BAD_REQUEST);
    }
    if (!direction.equals("left") && !direction.equals("right")) {
      return new ResponseEntity<String>("invalid inputs", HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<String>("swipe successful", HttpStatus.CREATED);
  }

}

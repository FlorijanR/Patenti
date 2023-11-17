package hr.fer.patenti.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hr.fer.patenti.domain.Result;
import hr.fer.patenti.service.MyService;

@RestController
@RequestMapping("/")
public class Controller {
	@Autowired
	private MyService myService;

	@PostMapping("")
	public Result req(@RequestBody PatentDTO info) {
		return myService.req(info);
	}
}

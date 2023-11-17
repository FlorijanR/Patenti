package hr.fer.patenti.service;

import hr.fer.patenti.domain.Result;
import hr.fer.patenti.rest.PatentDTO;

public interface MyService {
	Result req(PatentDTO info);
}

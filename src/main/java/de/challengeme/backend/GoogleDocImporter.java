package de.challengeme.backend;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import de.challengeme.backend.challenge.Challenge;
import de.challengeme.backend.challenge.ChallengeService;
import de.challengeme.backend.user.User;
import de.challengeme.backend.user.UserService;

@Configuration
public class GoogleDocImporter {

	private static Logger logger = LogManager.getLogger();

	@Autowired
	private ChallengeService challengeService;

	@Autowired
	private UserService userService;

	public void importChallenges(String excelFilePath) {

		User user = userService.getRootUser();

		try {

			Path tmpFile = Files.createTempFile("xlsx", "xlsx");
			Files.copy(Paths.get(excelFilePath), tmpFile, StandardCopyOption.REPLACE_EXISTING);

			try (XSSFWorkbook wb = new XSSFWorkbook(tmpFile.toFile())) {
				wb.setMissingCellPolicy(MissingCellPolicy.CREATE_NULL_AS_BLANK);
				XSSFSheet challengesSheet = wb.getSheet("Challenges");
				int rowIndex = 0;
				XSSFRow row;
				while ((row = challengesSheet.getRow(rowIndex++)) != null) {
					Challenge challenge = new Challenge();
					challenge.setCategory(mapCategory(row.getCell(0).toString()));
					if (challenge.getCategory() != null) {
						challenge.setTitle(row.getCell(1).toString());
						challenge.setDescription(row.getCell(2).toString());

						challenge.setCreatedByImport(true);
						challengeService.createChallenge(user, challenge);
						logger.info("Created challenge {}.", challenge.getId());
					}
				}
			}

			Files.delete(tmpFile);

		} catch (Exception e) {
			logger.error("Error importing challenges", e);
		}
	}

	private String mapCategory(String category) {
		switch (category) {
			case "@ home" :
				return "atHome";
			case "Beweg' dich!" :
				return "move";
			case "Do it for yourself" :
				return "dify";
			case "Eco" :
				return "eco";
			case "Fun" :
				return "fun";
			case "Know-How?" :
				return "knowledge";
			case "Kreativer Kopf" :
				return "creative";
			case "Raus aus der Komfort - Zone!" :
				return "noComfort";
			case "Roberts Koch Institut" :
				return "cooking";
			case "Wir - Voll sozial" :
				return "social";
		}
		return null;
	}

}

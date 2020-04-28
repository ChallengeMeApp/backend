package de.challengeme.backend;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.challengeme.backend.challenge.Category;
import de.challengeme.backend.challenge.Challenge;
import de.challengeme.backend.challenge.ChallengeKind;
import de.challengeme.backend.challenge.ChallengeService;
import de.challengeme.backend.user.MyUser;
import de.challengeme.backend.user.UserService;

@Configuration
public class GoogleDocImporter {

	private static Logger logger = LogManager.getLogger();

	@Autowired
	private ChallengeService challengeService;

	@Autowired
	private UserService userService;

	public void importChallenges(String excelFilePath) {

		MyUser user = userService.getRootUser();

		try {

			Path tmpFile = Files.createTempFile("xlsx", "xlsx");
			Files.copy(Paths.get(excelFilePath), tmpFile, StandardCopyOption.REPLACE_EXISTING);

			try (XSSFWorkbook wb = new XSSFWorkbook(tmpFile.toFile())) {
				wb.setMissingCellPolicy(MissingCellPolicy.CREATE_NULL_AS_BLANK);
				XSSFSheet challengesSheet = wb.getSheet("Challenges");
				int rowIndex = 6;
				XSSFRow row;
				Set<Long> challengesToDelete = Sets.newHashSet();
				for (Challenge challenge : challengeService.getImportedChallenges()) {
					challengesToDelete.add(challenge.getId());
				}

				while ((row = challengesSheet.getRow(rowIndex++)) != null) {

					String category = row.getCell(0).toString();
					String title = row.getCell(1).toString();
					if (!Strings.isNullOrEmpty(category) && !Strings.isNullOrEmpty(title)) {
						try {

							Challenge challenge = challengeService.getImportedChallengeFromTitle(title);
							if (challenge == null) {
								challenge = new Challenge();
							} else {
								challengesToDelete.remove(challenge.getId());
							}
							challenge.setCategory(mapCategory(category));
							if (challenge.getCategory() != null) {
								challenge.setTitle(title);
								challenge.setDescription(row.getCell(3).toString());
								challenge.setKind(mapChallengeKind(row.getCell(4).toString()));

								if (row.getCell(7).toString().toLowerCase().contains("j")) {
									try {
										challenge.setRepeatableAfterDays((int) (Double.parseDouble(row.getCell(8).toString())));
									} catch (NumberFormatException e) {
										// not repeatable
									}
								}
								challenge.setMaterial(mapMaterial(row.getCell(9).toString(), row.getCell(10).toString()));

								String duration = row.getCell(2).toString();
								if (!Strings.isNullOrEmpty(duration)) {
									try {
										challenge.setDurationSeconds((long) (Double.parseDouble(duration) * 60));
									} catch (NumberFormatException e) { // "as long as possible"
										challenge.setDurationSeconds(-1l);
									}
								}

								int pointsWin = 0;
								int pointsLoose = 0;

								String points = row.getCell(15).toString();
								if (!Strings.isNullOrEmpty(points)) {
									pointsWin = (int) Double.parseDouble(points);
									pointsLoose = pointsWin;
								}

								points = row.getCell(13).toString();
								if (!Strings.isNullOrEmpty(points)) {
									pointsWin += (int) Double.parseDouble(points);
								}

								points = row.getCell(14).toString();
								if (!Strings.isNullOrEmpty(points)) {
									pointsLoose += (int) Double.parseDouble(points);
								}

								challenge.setPointsWin(pointsWin);
								challenge.setPointsLoose(pointsLoose);

								challenge.setAddToTreasureChest(row.getCell(16).toString().toLowerCase().contains("j"));
								challenge.setCreatedByImport(true);

								challengeService.createChallenge(user, challenge);
								logger.info("Created challenge {}.", challenge.getId());
							} else {
								logger.warn("Category '{}' not found or title '{}' empty, skipping import.", category, title);
							}
						} catch (Exception e) {
							logger.error("Row: {}, Title: '{}', Error:", rowIndex, title, e);
						}
					}

				}

				logger.info("Deleting previously imported challenges, which are not in the file anymore.");
				for (Long id : challengesToDelete) {
					challengeService.deleteChallenge(id);
				}
			}

			Files.delete(tmpFile);

			logger.info("Creation succeeded, exiting.");
			System.exit(0);
		} catch (Exception e) {
			logger.error("Error importing challenges", e);
			System.exit(1);
		}
	}

	private String mapMaterial(String... material) {
		List<String> result = Lists.newArrayList(material);
		for (int index = result.size() - 1; index >= 0; index--) {
			if (Strings.isNullOrEmpty(result.get(index))) {
				result.remove(index);
			}
		}
		return Joiner.on(", ").join(result);
	}

	private ChallengeKind mapChallengeKind(String kind) {
		kind = kind.toLowerCase();
		if (kind.startsWith("fremd")) {
			return ChallengeKind.competitive;
		}
		if (kind.startsWith("gemein")) {
			return ChallengeKind.together;
		}
		return ChallengeKind.self;
	}

	private Category mapCategory(String category) {
		switch (category) {
			case "@ home" :
				return Category.household;
			case "Beweg' dich!" :
				return Category.physical;
			case "Eco" :
				return Category.eco;
			case "Fun" :
				return Category.fun;
			case "Know-How?" :
				return Category.education;
		}
		if (category.startsWith("Wir")) {
			return Category.social;
		}
		if (category.startsWith("Robert")) {
			return Category.cooking;
		}
		if (category.startsWith("Raus aus der")) {
			return Category.noComfortZone;
		}
		if (category.toLowerCase().contains("kreativ")) {
			return Category.creative;
		}
		if (category.toLowerCase().contains("do")) {
			return Category.selfcare;
		}
		if (category.toLowerCase().contains("haus")) {
			return Category.household;
		}
		return null;
	}

}

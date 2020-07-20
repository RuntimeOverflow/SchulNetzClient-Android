package com.runtimeoverflow.SchulNetzClient;

import com.runtimeoverflow.SchulNetzClient.Data.Grade;
import com.runtimeoverflow.SchulNetzClient.Data.Student;
import com.runtimeoverflow.SchulNetzClient.Data.Subject;
import com.runtimeoverflow.SchulNetzClient.Data.Teacher;
import com.runtimeoverflow.SchulNetzClient.Data.Transaction;
import com.runtimeoverflow.SchulNetzClient.Data.User;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

public class Parser {
	public static boolean parseGrades(Document doc, User user){
		try{
			Elements dataTables = doc.getElementsByClass("mdl-data-table");
			if(dataTables.size() <= 0 || dataTables.get(0).children().size() <= 0) return false;
			Element tableBody = dataTables.get(0).child(0);

			if(tableBody.children().size() <= 0) return false;
			Elements rows = tableBody.children();
			rows.remove(0);

			if(rows.size() % 3 != 0) return false;
			for(int i = 0; i < rows.size(); i += 3){
				Element mainRow = rows.get(i);
				Element gradesRow = rows.get(i + 1);

				if(mainRow.children().size() < 5 || mainRow.children().get(0).children().size() <= 0 || !mainRow.children().get(0).hasText() || !mainRow.children().get(0).children().get(0).hasText()) continue;
				Subject s = user.subjectForIdentifier(mainRow.children().get(0).children().get(0).ownText().trim());
				s.name = mainRow.children().get(0).ownText().trim();
				s.confirmed = mainRow.children().get(4).children().size() <= 0 || !mainRow.children().get(4).children().get(0).hasAttr("href");

				if(gradesRow.children().size() <= 0) return false;
				if(gradesRow.children().get(0).children().size() > 0 && gradesRow.children().get(0).children().get(0).children().size() > 0){
					Element gradesTable = gradesRow.children().get(0).children().get(0).children().get(0);
					if(gradesTable.children().size() <= 0) continue;
					Elements gradeRows = gradesTable.children();
					gradeRows.remove(0);

					for(Element gradeRow : gradeRows){
						if(gradeRow.children().size() < 4) continue;
						Grade g = new Grade();

						if(gradeRow.children().get(0).hasText()) {
							SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
							g.date = Calendar.getInstance();
							g.date.setTime(sdf.parse(gradeRow.children().get(0).ownText().trim()));
						}

						if(gradeRow.children().get(1).hasText()) {
							g.content = gradeRow.children().get(1).ownText().trim();
						}

						if(gradeRow.children().get(2).hasText()) {
							g.grade = Double.parseDouble(gradeRow.children().get(2).ownText().trim());

							if(gradeRow.children().get(2).children().size() >= 2 && gradeRow.children().get(2).children().get(1).hasText()) {
								g.details = gradeRow.children().get(2).children().get(1).ownText().trim();
							}
						}

						if(gradeRow.children().get(3).hasText()) {
							g.weight = Double.parseDouble(gradeRow.children().get(3).ownText().trim());
						}

						s.grades.add(g);
					}
				}
			}
		} catch(Exception e){
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static boolean parseSubjects(Document doc, User user){
		try{
			Element list = doc.getElementById("clsList");
			if(list == null) return false;

			for(Element subject : list.children()){
				Subject s = new Subject();
				if(subject.getElementsByClass("mdl-radio__label").size() <= 0) return false;
				s.identifier = subject.getElementsByClass("mdl-radio__label").get(0).ownText().trim();
				user.subjects.add(s);
			}
		} catch(Exception e){
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static boolean parseStudents(Document doc, User user){
		try{
			Element table = doc.getElementById("cls-table-Kursliste");
			if(table == null || table.children().size() < 2) return false;

			Elements rows = table.children().get(1).children();
			if(rows.size() <= 2) return false;
			rows.remove(0);
			rows.remove(0);

			for(Element row : rows){
				if(row.children().size() < 15) continue;

				Student s = new Student();
				s.lastName = row.children().get(1).ownText().trim();
				s.firstName = row.children().get(2).ownText().trim();
				s.gender = Objects.equals(row.children().get(3).ownText().trim().toLowerCase(), "w".toLowerCase());
				s.degree = row.children().get(4).ownText().trim();
				s.bilingual = Objects.equals(row.children().get(5).ownText().trim().toLowerCase(), "b".toLowerCase());
				s.className = row.children().get(6).ownText().trim();
				s.address = row.children().get(7).ownText().trim();
				s.zipCode = row.children().get(8).ownText().trim().length() > 0 ? Integer.parseInt(row.children().get(8).ownText().trim()) : -1;
				s.city = row.children().get(9).ownText().trim();
				s.phone = row.children().get(10).ownText().trim();
				if(row.children().get(11).ownText().trim().length() > 0){
					SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
					s.dateOfBirth = Calendar.getInstance();
					s.dateOfBirth.setTime(sdf.parse(row.children().get(11).ownText().trim()));
				}
				s.additionalClasses = row.children().get(12).ownText().trim();
				s.status = row.children().get(13).ownText().trim();
				s.placeOfWork = row.children().get(14).ownText().trim();

				user.students.add(s);
			}
		} catch(Exception e){
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static boolean parseTeachers(Document doc, User user){
		try{
			Element table = doc.getElementById("cls-table-Lehrerliste");
			if(table == null || table.children().size() < 2) return false;

			Elements rows = table.children().get(1).children();
			if(rows.size() <= 2) return false;
			rows.remove(0);
			rows.remove(0);

			for(Element row : rows){
				if(row.children().size() < 5) continue;

				Teacher t = new Teacher();
				t.lastName = row.children().get(1).ownText().trim();
				t.firstName = row.children().get(2).ownText().trim();
				t.shortName = row.children().get(3).ownText().trim();
				t.mail = row.children().get(1).getElementsByTag("a").size() > 0 ? row.children().get(1).getElementsByTag("a").get(0).ownText().trim() : "";

				user.teachers.add(t);
			}
		} catch(Exception e){
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static boolean parseSelf(Document doc, User user){
		try{
			Element card = doc.getElementById("content-card");
			if(card.getElementsByTag("table").size() < 2 || card.getElementsByTag("table").get(0).children().size() <= 0) return false;

			Element tableBody = card.getElementsByTag("table").get(0).children().get(0);
			if(tableBody.children().size() < 2 || tableBody.children().get(0).children().size() < 2 || tableBody.children().get(1).children().size() < 2) return false;
			String lastName = tableBody.children().get(0).children().get(1).ownText().trim();
			String firstName = tableBody.children().get(1).children().get(1).ownText().trim();

			for(Student s : user.students){
				if(Objects.equals(s.firstName.toLowerCase(), firstName.toLowerCase()) && Objects.equals(s.lastName.toLowerCase(), lastName.toLowerCase())){
					s.self = true;
					break;
				}
			}

			if(user.self == null) return false;
		} catch(Exception e){
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static boolean parseTransactions(Document doc, User user){
		try{
			Element card = doc.getElementById("content-card");
			if(card.getElementsByTag("table").size() < 2 || card.getElementsByTag("table").get(1).children().size() <= 0) return false;

			Element tableBody = card.getElementsByTag("table").get(1).children().get(0);
			if(tableBody.children().size() < 2) return false;
			else if(tableBody.children().size() == 2) return true;

			Elements rows = tableBody.children();
			rows.remove(0);
			rows.remove(rows.size() - 1);

			for(Element row : rows){
				if(row.children().size() < 4) continue;

				Transaction t = new Transaction();

				if(row.children().get(0).ownText().trim().length() > 0){
					SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
					t.date = Calendar.getInstance();
					t.date.setTime(sdf.parse(row.children().get(0).ownText().trim()));
				}

				t.reason = row.children().get(1).ownText().trim();
				if(row.children().get(2).children().size() > 0){
					t.amount = Double.parseDouble(row.children().get(2).children().get(0).ownText().trim().replace("sFr", ""));
				}

				user.transactions.add(t);
			}

			if(card.getElementsByTag("p").size() <= 0) return false;
			user.balanceConfirmed = card.getElementsByTag("p").get(0).getElementsByTag("a").size() <= 0;
		} catch(Exception e){
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static boolean isSignedIn(Document doc){
		return doc.getElementById("nav-main-menu") != null;
	}
}
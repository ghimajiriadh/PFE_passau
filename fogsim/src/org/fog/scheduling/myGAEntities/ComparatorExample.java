package org.fog.scheduling.myGAEntities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;



public class ComparatorExample {
	public static void main(String[] args) {
		
		MyPopulation population = new MyPopulation(10, 5, 20);
		population.printPopulation();
		System.out.println("\\\\\\\\\\\\\\\\\\\\");
		population.sortPopulation();
		population.printPopulation();
		
		
		
//        // create list students
//        List<Student> listStudents = new ArrayList<Student>();
//        // add students to list
//        listStudents.add(new Student(1, "Vinh", 19, "Hanoi"));
//        listStudents.add(new Student(2, "Hoa", 24, "Hanoi"));
//        listStudents.add(new Student(3, "Phu", 20, "Hanoi"));
//        listStudents.add(new Student(4, "Quy", 22, "Hanoi"));
//         
//        // sort list student by it's name ASC
//        System.out.println("sort list student by it's name ASC: ");
//        Collections.sort(listStudents, new Comparator<Student>() {
//            @Override
//            public int compare(Student o1, Student o2) {
//                return o1.getName().compareTo(o2.getName());
//            }
//        });
//        // show list students
//        for (Student student : listStudents) {
//            System.out.println(student.toString());
//        }
//         
//        // sort list student by it's age ASC
//        System.out.println("sort list student by it's age ASC: ");
//        Collections.sort(listStudents, new Comparator<Student>() {
//            @Override
//            public int compare(Student o1, Student o2) {
//                return o1.getAge() > o2.getAge() ? -1 : 1;
//            }
//        });
//        // show list students
//        for (Student student : listStudents) {
//            System.out.println(student.toString());
//        }
    }

}



class Student {
    private int id;
    private String name;
    private int age;
    private String address;
 
    public Student() {
    }
 
    public Student(int id, String name, int age, String address) {
        super();
        this.id = id;
        this.name = name;
        this.age = age;
        this.address = address;
    }
 
    // getter & setter
     
    @Override
    public String toString() {
        return "Student@id=" + id + ",name=" + name 
                + ",age=" + age + ",address=" + address;
    }
    
    public String getName() {
    	return name;
    }
    
    public int getAge() {
    	return age;
    }
    
}


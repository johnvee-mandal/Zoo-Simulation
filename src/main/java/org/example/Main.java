import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Zoo zoo = new Zoo();
        zoo.initializeDefaultState();

        while (true) {
            System.out.println("\n\n=== WELCOME TO THE ZOO SIMULATION ===");
            System.out.println("1. Administrator Console");
            System.out.println("2. Visitor Ticketing & Entry");
            System.out.println("3. Exit Simulation");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    AdminConsole adminConsole = new AdminConsole(zoo, scanner);
                    adminConsole.start();
                    break;
                case "2":
                    TicketingModule ticketingModule = new TicketingModule(zoo, scanner);
                    Visitor visitor = ticketingModule.start();
                    if (visitor != null) {
                        System.out.println("\n=== Visitor Entry ===");
                        System.out.print("Enter your ticket code: ");
                        String enteredCode = scanner.nextLine();
                        if (zoo.validateTicket(enteredCode)) {
                            System.out.println("Welcome, " + visitor.getName() + "! Enjoy your visit.");
                            VisitorModule visitorModule = new VisitorModule(visitor, zoo, scanner);
                            visitorModule.start();
                        } else {
                            System.out.println("Invalid ticket code. Entry denied.");
                        }
                    }
                    break;
                case "3":
                    System.out.println("Exiting simulation. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
}

class AdminConsole {
    private Zoo zoo;
    private Scanner scanner;

    public AdminConsole(Zoo zoo, Scanner scanner) {
        this.zoo = zoo;
        this.scanner = scanner;
    }

    public void start() {
        System.out.println("\n=== Welcome to the Zoo Admin Console ===");
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if ("admin".equals(username) && "adminadmin".equals(password)) {
            System.out.println("Login successful. Welcome!");
            showAdminMenu();
        } else {
            System.out.println("Login failed. Returning to main menu.");
        }
    }

    private void showAdminMenu() {
        while (true) {
            System.out.println("\n========== \uD83D\uDC2F ZOO ADMIN MAIN MENU ==========");
            System.out.println("1. Setup Zoo Staff");
            System.out.println("2. Access Handler Module");
            System.out.println("3. Open Zoo to Visitors");
            System.out.println("4. Close Zoo to Visitors");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    setupStaff();
                    break;
                case "2":
                    accessHandlerModule();
                    break;
                case "3":
                    zoo.setOpen(true);
                    System.out.println("The zoo is now open to visitors.");
                    break;
                case "4":
                    zoo.setOpen(false);
                    System.out.println("The zoo is now closed to visitors.");
                    break;
                case "5":
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void setupStaff() {
        System.out.println("\n--- Zoo Setup ---");
        System.out.print("Enter your name, Manager: ");
        zoo.setManager(new Manager(scanner.nextLine(), null));
        System.out.print("Enter Veterinarian's name: ");
        zoo.setVeterinarian(new Veterinarian(scanner.nextLine(), zoo.getHospital()));

        for(Building b : zoo.getBuildings()){
            if(b instanceof Enclosure){
                System.out.print("Enter Handler for " + b.getName() + ": ");
                zoo.addPerson(new Handler(scanner.nextLine(), b));
            }
            if(b instanceof TicketShop){
                System.out.print("Enter Vendor for Ticket Shop: ");
                zoo.addPerson(new Vendor(scanner.nextLine(), b));
            } else if (b instanceof Shop && !(b instanceof TicketShop)){
                System.out.print("Enter Vendor for " + b.getName() + ": ");
                zoo.addPerson(new Vendor(scanner.nextLine(), b));
            }
        }
        System.out.println("Zoo staff setup complete.");
    }

    private void accessHandlerModule() {
        System.out.print("Enter your name (Handler): ");
        String name = scanner.nextLine();
        Handler handler = zoo.findHandlerByName(name);

        if (handler == null) {
            System.out.println("Handler not found.");
            return;
        }

        System.out.println("Welcome, Handler " + handler.getName() + "!");
        HandlerModule handlerModule = new HandlerModule(handler, zoo, scanner);
        handlerModule.start();
    }
}

class HandlerModule {
    private Handler handler;
    private Zoo zoo;
    private Scanner scanner;
    private List<Animal> assignedAnimals;

    public HandlerModule(Handler handler, Zoo zoo, Scanner scanner) {
        this.handler = handler;
        this.zoo = zoo;
        this.scanner = scanner;
        this.assignedAnimals = zoo.getAnimalsInEnclosure((Enclosure) handler.getLocation());
    }

    public void start() {
        while (true) {
            System.out.println("\n--- Animal Duty Menu ---");
            System.out.println("Animals assigned to you:");
            for (int i = 0; i < assignedAnimals.size(); i++) {
                System.out.println((i + 1) + ". " + assignedAnimals.get(i).getName());
            }
            System.out.print("Choose animal number to interact with (0 to exit): ");
            int choice = Integer.parseInt(scanner.nextLine());

            if (choice == 0) {
                System.out.println("Finished duties for the day.");
                return;
            }
            if (choice > 0 && choice <= assignedAnimals.size()) {
                interactWithAnimal(assignedAnimals.get(choice - 1));
            } else {
                System.out.println("Invalid animal number.");
            }
        }
    }

    private void interactWithAnimal(Animal animal) {
        System.out.println("\nChoose action:");
        System.out.println("1. Feed " + animal.getName());
        System.out.println("2. Exercise " + animal.getName());
        System.out.println("3. Examine " + animal.getName() + " and send to Vet");
        System.out.print("Choose an option: ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                handler.feed(animal);
                break;
            case "2":
                handler.exercise(animal);
                break;
            case "3":
                System.out.println("Sending to Hospital...");
                handler.examine(animal, zoo);
                System.out.println(animal.getName() + " admitted at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                break;
            default:
                System.out.println("Invalid action.");
        }
    }
}

class TicketingModule {
    private Zoo zoo;
    private Scanner scanner;

    public TicketingModule(Zoo zoo, Scanner scanner) {
        this.zoo = zoo;
        this.scanner = scanner;
    }

    public Visitor start() {
        System.out.println("\n=== \uD83C\uDFAB WELCOME TO THE ZOO TICKET SHOP ===");
        System.out.println("Here's what you can experience in the zoo:");
        System.out.println("Visit Animal Enclosures (Elephant, Lion, Owl)");
        System.out.println("Buy snacks and drinks from our Shops");
        System.out.println("Listen to science lectures at the Hospital");
        System.out.println("Buy fun gifts at our Gift Shop");

        System.out.print("\nWould you like to buy a ticket? (yes/no): ");
        if (!scanner.nextLine().equalsIgnoreCase("yes")) {
            return null;
        }

        System.out.print("Enter your name: ");
        String name = scanner.nextLine();
        System.out.print("Enter your age: ");
        int age = Integer.parseInt(scanner.nextLine());

        String ticketType = getTicketType(age);
        double price = getTicketPrice(ticketType);

        System.out.println("\nYou qualify for a " + ticketType.toUpperCase() + " ticket.");
        System.out.printf("Ticket Price: P%.2f\n", price);
        System.out.print("Proceed with purchase? (yes/no): ");
        if (!scanner.nextLine().equalsIgnoreCase("yes")) {
            System.out.println("Purchase cancelled.");
            return null;
        }

        String ticketCode = "ZOO-" + (new Random().nextInt(9000) + 1000);
        System.out.println("Ticket purchased!");
        System.out.println("Your ticket code is: " + ticketCode);
        System.out.println("[Ticket added to system]");

        Visitor visitor = new Visitor(name, zoo.getTicketShop());
        visitor.setAge(age);
        visitor.setTicketCode(ticketCode);
        zoo.addPerson(visitor);
        zoo.addValidTicket(ticketCode);

        return visitor;
    }

    private String getTicketType(int age) {
        if (age <= 5) return "Child";
        if (age <= 17) return "Student";
        if (age <= 59) return "Adult";
        return "Senior";
    }

    private double getTicketPrice(String ticketType) {
        switch (ticketType) {
            case "Child": return 0.00;
            case "Student": return 75.00;
            case "Adult": return 150.00;
            case "Senior": return 50.00;
            default: return 0.00;
        }
    }
}

class VisitorModule {
    private Visitor visitor;
    private Zoo zoo;
    private Scanner scanner;

    public VisitorModule(Visitor visitor, Zoo zoo, Scanner scanner) {
        this.visitor = visitor;
        this.zoo = zoo;
        this.scanner = scanner;
    }

    public void start() {
        if (!zoo.isOpen()) {
            System.out.println("Sorry, the zoo is currently closed.");
            return;
        }
        while (true) {
            System.out.println("\nWhat would you like to do?");
            System.out.println("1. Visit Enclosure");
            System.out.println("2. Visit Shop");
            System.out.println("3. Visit Hospital");
            System.out.println("4. Leave Zoo");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    visitEnclosure();
                    break;
                case "2":
                    visitShop();
                    break;
                case "3":
                    visitHospital();
                    break;
                case "4":
                    System.out.println("You have left the zoo. \uD83D\uDC4B");
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void visitEnclosure() {
        System.out.println("\n==Zoo Enclosure==");
        System.out.println("Choose Enclosure:");
        List<Enclosure> enclosures = zoo.getEnclosures();
        for (int i = 0; i < enclosures.size(); i++) {
            System.out.println((i + 1) + ". " + enclosures.get(i).getName());
        }
        System.out.print("Choose an option: ");
        int choice = Integer.parseInt(scanner.nextLine()) - 1;

        if (choice >= 0 && choice < enclosures.size()) {
            Enclosure selectedEnclosure = enclosures.get(choice);
            visitor.goTo(selectedEnclosure);
            List<Animal> animals = zoo.getAnimalsInEnclosure(selectedEnclosure);
            if (!animals.isEmpty()) {
                Animal animalToSee = animals.get(0); // See the first animal
                System.out.println("You see " + animalToSee.getName() + "!");
                animalToSee.makeSound();
                System.out.print("Would you like to feed " + animalToSee.getName() + "? (yes/no): ");
                if (scanner.nextLine().equalsIgnoreCase("yes")) {
                    animalToSee.eat();
                }
            } else {
                System.out.println("This enclosure is empty right now.");
            }
        } else {
            System.out.println("Invalid choice.");
        }
    }

    private void visitShop() {
        System.out.println("\n=== \uD83D\uDED2 Zoo Shop ===");
        Shop shop = zoo.getFoodShop(); // Simplified to one main shop
        visitor.goTo(shop);

        List<Product> products = shop.getProducts();
        List<Product> cart = new ArrayList<>();
        double total = 0;

        while(true){
            System.out.println("Available Products:");
            for (int i = 0; i < products.size(); i++) {
                System.out.printf("%d. %s - P%.2f\n", i + 1, products.get(i).getName(), products.get(i).getPrice());
            }
            System.out.print("Enter the numbers of the items you want to buy (e.g., 1 3, or 0 to finish): ");
            String[] choices = scanner.nextLine().split(" ");

            if(choices[0].equals("0")) break;

            for(String c : choices){
                int itemNum = Integer.parseInt(c) - 1;
                if(itemNum >= 0 && itemNum < products.size()){
                    Product p = products.get(itemNum);
                    cart.add(p);
                    total += p.getPrice();
                    System.out.println("Added: " + p.getName());
                }
            }
        }

        if(cart.isEmpty()) return;

        System.out.println("\nSelected:");
        for(Product p : cart){
            System.out.printf("- %s (P%.2f)\n", p.getName(), p.getPrice());
        }
        System.out.printf("Total: P%.2f\n", total);
        System.out.print("Proceed to checkout? (yes/no): ");
        if(scanner.nextLine().equalsIgnoreCase("yes")){
            System.out.println("Payment successful!");
            System.out.println("Receipt:");
            for(Product p : cart){
                System.out.printf("- %s: P%.2f\n", p.getName(), p.getPrice());
            }
            System.out.printf("Total Paid: P%.2f\n", total);
        } else {
            System.out.println("Purchase cancelled.");
        }
    }

    private void visitHospital() {
        Hospital hospital = zoo.getHospital();
        visitor.goTo(hospital);
        Veterinarian vet = zoo.getVeterinarian();

        while (true) {
            System.out.println("\n=== \uD83C\uDFE5 Zoo Visitor Hospital Monitor ===");
            System.out.println("1. View Sick Animals");
            System.out.println("2. View Healed Animals");
            System.out.println("3. Attend Science Lecture");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.println("\n* Sick Animals Currently in Hospital:");
                    List<Animal> sick = hospital.getSickAnimals();
                    if(sick.isEmpty()) System.out.println("- None");
                    else sick.forEach(a -> System.out.println("- " + a.getName()));
                    break;
                case "2":
                    System.out.println("\n\u2695 Healed Animals with Timestamps:");
                    List<String> healedLog = hospital.getHealedAnimalLog();
                    if(healedLog.isEmpty()) System.out.println("- None");
                    else healedLog.forEach(System.out::println);
                    break;
                case "3":
                    if(vet != null) vet.lecture();
                    else System.out.println("There is no veterinarian available for a lecture today.");
                    break;
                case "4":
                    System.out.println("Exiting Zoo Vet Hospital. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }
}


class Zoo {
    private List<Animal> animals = new ArrayList<>();
    private List<Person> people = new ArrayList<>();
    private List<Building> buildings = new ArrayList<>();
    private List<String> validTicketCodes = new ArrayList<>();
    private Manager manager;
    private Veterinarian veterinarian;
    private boolean isOpen = false;

    public void initializeDefaultState() {
        // Buildings
        Enclosure felineEnclosure = new Enclosure("Feline Enclosure");
        Enclosure pachydermEnclosure = new Enclosure("Pachyderm Enclosure");
        Enclosure birdEnclosure = new Enclosure("Bird Enclosure");
        Hospital hospital = new Hospital();
        Shop ticketShop = new TicketShop();
        Shop foodShop = new FoodShop();

        this.addBuilding(felineEnclosure);
        this.addBuilding(pachydermEnclosure);
        this.addBuilding(birdEnclosure);
        this.addBuilding(hospital);
        this.addBuilding(ticketShop);
        this.addBuilding(foodShop);

        // Animals
        this.addAnimal(new Tiger("Mufasa", felineEnclosure));
        this.addAnimal(new Lion("Simba", felineEnclosure));
        this.addAnimal(new Elephant("Dumbo", pachydermEnclosure));
        this.addAnimal(new Owl("Hedwig", birdEnclosure));

        // Default Staff
        this.setManager(new Manager("Mr. Hammond", null));
        this.setVeterinarian(new Veterinarian("Dr. Ellie", hospital));
        this.addPerson(new Handler("Claire", felineEnclosure));
        this.addPerson(new Handler("Robert", pachydermEnclosure));
        this.addPerson(new Handler("Jack", birdEnclosure));
        this.addPerson(new Vendor("Lisa", ticketShop));
        this.addPerson(new Vendor("Tommy", foodShop));
    }

    public void addAnimal(Animal animal) { this.animals.add(animal); }
    public void addPerson(Person person) { this.people.add(person); }
    public void addBuilding(Building building) { this.buildings.add(building); }
    public void addValidTicket(String code) { this.validTicketCodes.add(code); }
    public boolean validateTicket(String code) { return this.validTicketCodes.contains(code); }

    public List<Building> getBuildings() { return buildings; }
    public List<Enclosure> getEnclosures() {
        return buildings.stream()
                .filter(b -> b instanceof Enclosure)
                .map(b -> (Enclosure) b)
                .collect(Collectors.toList());
    }
    public Hospital getHospital() {
        return (Hospital) buildings.stream().filter(b -> b instanceof Hospital).findFirst().orElse(null);
    }
    public Shop getTicketShop() {
        return (Shop) buildings.stream().filter(b -> b instanceof TicketShop).findFirst().orElse(null);
    }
    public Shop getFoodShop() {
        return (Shop) buildings.stream().filter(b -> b instanceof FoodShop).findFirst().orElse(null);
    }
    public List<Animal> getAnimalsInEnclosure(Enclosure enclosure) {
        return animals.stream()
                .filter(a -> a.getLocation().equals(enclosure))
                .collect(Collectors.toList());
    }
    public Handler findHandlerByName(String name) {
        return people.stream()
                .filter(p -> p instanceof Handler && p.getName().equalsIgnoreCase(name))
                .map(p -> (Handler) p)
                .findFirst().orElse(null);
    }

    public void setManager(Manager m) { this.manager = m; addPerson(m); }
    public void setVeterinarian(Veterinarian v) { this.veterinarian = v; addPerson(v); }
    public Veterinarian getVeterinarian() { return veterinarian; }
    public boolean isOpen() { return isOpen; }
    public void setOpen(boolean open) { isOpen = open; }
}

class Product {
    private String name;
    private double price;
    public Product(String name, double price) { this.name = name; this.price = price; }
    public String getName() { return name; }
    public double getPrice() { return price; }
}

abstract class Building {
    protected String name;
    public String getName() { return name; }
}
class Enclosure extends Building {
    public Enclosure(String name) { this.name = name; }
}
class Hospital extends Building {
    private List<Animal> sickAnimals = new ArrayList<>();
    private List<String> healedAnimalLog = new ArrayList<>();
    public Hospital() { this.name = "Animal Hospital"; }
    public void admitAnimal(Animal animal) { sickAnimals.add(animal); }
    public void dischargeAnimal(Animal animal) { sickAnimals.remove(animal); }
    public List<Animal> getSickAnimals() { return sickAnimals; }
    public void logHealedAnimal(String log) { healedAnimalLog.add(log); }
    public List<String> getHealedAnimalLog() { return healedAnimalLog; }
}
abstract class Shop extends Building {
    protected List<Product> products = new ArrayList<>();
    public List<Product> getProducts() { return products; }
}
class TicketShop extends Shop {
    public TicketShop() { this.name = "Ticket Shop"; }
}
class FoodShop extends Shop {
    public FoodShop() {
        this.name = "Food Shop";
        products.add(new Product("Soft Drink", 30));
        products.add(new Product("Popcorn", 50));
        products.add(new Product("Plush Toy", 120));
        products.add(new Product("Keychain", 45));
    }
}

abstract class Person {
    protected String name;
    protected Building location;
    public Person(String name, Building location) { this.name = name; this.location = location; }
    public String getName() { return name; }
    public Building getLocation() { return location; }
    public void goTo(Building destination) {
        this.location = destination;
        System.out.println(this.name + " is moving to " + destination.getName() + ".");
    }
}
class Manager extends Person {
    public Manager(String name, Building location) { super(name, location); }
    public void openZoo() { System.out.println("Manager " + name + " has opened the zoo! Welcome!"); }
    public void closeZoo() { System.out.println("Manager " + name + " has closed the zoo! Please come again."); }
}
class Handler extends Person {
    public Handler(String name, Building location) { super(name, location); }
    public void feed(Animal animal) { System.out.println(name + " is feeding " + animal.getName() + "."); animal.eat(); }
    public void exercise(Animal animal) { System.out.println(name + " is exercising " + animal.getName() + "."); animal.roam(); }
    public void examine(Animal animal, Zoo zoo) {
        animal.setHealthy(false);
        Hospital hospital = zoo.getHospital();
        if(hospital != null){
            animal.setLocation(hospital);
            hospital.admitAnimal(animal);
        }
    }
}
class Vendor extends Person {
    public Vendor(String name, Building location) { super(name, location); }
    public void sell() { System.out.println(name + " at the " + location.getName() + " is selling goods."); }
}
class Visitor extends Person {
    private int age;
    private String ticketCode;
    public Visitor(String name, Building location) { super(name, location); }
    public void setAge(int age) { this.age = age; }
    public String getTicketCode() { return ticketCode; }
    public void setTicketCode(String code) { this.ticketCode = code; }
}
class Veterinarian extends Person {
    public Veterinarian(String name, Building location) { super(name, location); }
    public void heal(Animal animal) {
        animal.setHealthy(true);
        System.out.println("✅ Healed: " + animal.getName());
    }
    public void healAll(Hospital hospital){
        System.out.println("Dr. " + name + " begins healing sick animals...");
        List<Animal> toHeal = new ArrayList<>(hospital.getSickAnimals());
        for(Animal a : toHeal){
            heal(a);
            String log = String.format("✅ %s (%s)", a.getName(), LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            hospital.logHealedAnimal(log);
            System.out.println(a.getName() + " has been discharged and returned to enclosure.");
            a.setLocation(a.getOriginalEnclosure()); // Return to original home
            hospital.dischargeAnimal(a);
        }
    }
    public void lecture() { System.out.println("Dr. " + name + " gives a science lecture on animal health and conservation."); }
}

abstract class Animal {
    protected String name;
    protected boolean isHealthy;
    protected Building location;
    protected Enclosure originalEnclosure;
    public Animal(String name, Building location) {
        this.name = name;
        this.isHealthy = true;
        this.location = location;
        if(location instanceof Enclosure) this.originalEnclosure = (Enclosure) location;
    }
    public String getName() { return name; }
    public void setHealthy(boolean healthy) { isHealthy = healthy; }
    public Building getLocation() { return location; }
    public void setLocation(Building location) { this.location = location; }
    public Enclosure getOriginalEnclosure() { return originalEnclosure; }
    public void eat() { System.out.println(name + " is eating."); }
    public void sleep() { System.out.println(name + " is sleeping. Zzz..."); }
    public abstract void roam();
    public abstract void makeSound();
}
abstract class Feline extends Animal {
    public Feline(String name, Building location) { super(name, location); }
    @Override public void roam() { System.out.println(name + " is prowling gracefully."); }
}
class Tiger extends Feline {
    public Tiger(String name, Building location) { super(name, location); }
    @Override public void makeSound() { System.out.println(name + " roars! \uD83D\uDC2F"); }
}
class Lion extends Feline {
    public Lion(String name, Building location) { super(name, location); }
    @Override public void makeSound() { System.out.println(name + " roars! \uD83E\uDD81"); }
}
class Cheetah extends Feline {
    public Cheetah(String name, Building location) { super(name, location); }
    @Override public void makeSound() { System.out.println(name + " chirps!"); }
}
abstract class Pachyderm extends Animal {
    public Pachyderm(String name, Building location) { super(name, location); }
    @Override public void roam() { System.out.println(name + " is stomping around."); }
}
class Rhino extends Pachyderm {
    public Rhino(String name, Building location) { super(name, location); }
    @Override public void makeSound() { System.out.println(name + " snorts!"); }
}
class Elephant extends Pachyderm {
    public Elephant(String name, Building location) { super(name, location); }
    @Override public void makeSound() { System.out.println(name + " trumpets! \uD83D\uDC18"); }
}
class Hippo extends Pachyderm {
    public Hippo(String name, Building location) { super(name, location); }
    @Override public void makeSound() { System.out.println(name + " bellows!"); }
}
abstract class Bird extends Animal {
    public Bird(String name, Building location) { super(name, location); }
    @Override public void roam() { System.out.println(name + " is fluttering around."); }
}
class Parrot extends Bird {
    public Parrot(String name, Building location) { super(name, location); }
    @Override public void makeSound() { System.out.println(name + " squawks!"); }
}
class Falcon extends Bird {
    public Falcon(String name, Building location) { super(name, location); }
    @Override public void makeSound() { System.out.println(name + " screeches!"); }
}
class Owl extends Bird {
    public Owl(String name, Building location) { super(name, location); }
    @Override public void makeSound() { System.out.println(name + " hoots! \uD83E\uDD89"); }
}

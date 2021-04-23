package Main;

/**
 * Clasa care retine id-ul si numele userilor conectati la chat
 */
public class CurrentUser {

        private int id;
        private String name;

        public CurrentUser() {
        }

        public CurrentUser(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    @Override
    public String toString() {
        return "ID : " + id + " Nickname : " +name;
    }
}

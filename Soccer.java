import java.sql.* ;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;

class Soccer
{
    Scanner scanner;
    int sqlCode;
    String sqlState;
    Connection con;
    Statement statement;
    public Soccer() throws SQLException {
        this.scanner = new Scanner(System.in); //scanner for inputs!
        this.sqlCode=0;      // Variable to hold SQLCODE
        this.sqlState="00000";  // Variable to hold SQLSTATE

        // Register the driver.  You must register the driver before you can use it.
        try { DriverManager.registerDriver ( new com.ibm.db2.jcc.DB2Driver() ) ; }
        catch (Exception cnfe){ System.out.println("Class not found"); }

        // This is the url you must use for DB2.
        //Note: This url may not valid now ! Check for the correct year and semester and server name.
        String url = "jdbc:db2://winter2023-comp421.cs.mcgill.ca:50000/cs421";

        //REMEMBER to remove your user id and password before submitting your code!!
        String your_userid = null;
        String your_password = null;
//        TODO REMOVE!!

        //AS AN ALTERNATIVE, you can just set your password in the shell environment in the Unix (as shown below) and read it from there.
        //$  export SOCSPASSWD=yoursocspasswd
        if(your_userid == null && (your_userid = System.getenv("SOCSUSER")) == null)
        {
            System.err.println("Error!! do not have a password to connect to the database!");
            System.exit(1);
        }
        if(your_password == null && (your_password = System.getenv("SOCSPASSWD")) == null)
        {
            System.err.println("Error!! do not have a password to connect to the database!");
            System.exit(1);
        }
        this.con = DriverManager.getConnection (url,your_userid,your_password) ;
        this.statement = con.createStatement ( ) ;
    }

    public static void main ( String [ ] args ) throws SQLException
    {
        Soccer soccer = new Soccer();

        boolean repeat = true;
        while (repeat) {
            repeat = soccer.mainMenu();
        }

        soccer.statement.close ( ) ;
        soccer.con.close ( ) ;

        System.out.println("Exiting application");
    }

    public Boolean mainMenu() {
        System.out.println("Soccer Main Menu");
        System.out.println("\t 1. List information of matches of a country");
        System.out.println("\t 2. Insert initial player information for a match");
        System.out.println("\t 3. Insert goal");
        System.out.println("\t 4. Exit application");
        System.out.println("Please Enter Your Option: ");
        String input = this.scanner.nextLine();
        switch (input)
        {
            case "1":
                System.out.println("1. List information of matches of a country");
                countryMatchInfoMenu();
                return true;
            case "2":
                System.out.println("2. Insert initial player information for a match");
                insertPlayerMenu();
                return true;
            case "3":
                System.out.println("3. Insert goal");
                insertGoalMenu();
                return true;
            case "4":
                System.out.println("4. Exit application");
                return false;
            default:
                System.out.println("Invalid Option:((");
                System.out.println("Valid Options: [1, 2, 3, 4]");
                return true;
                //could loop back here, but I think they'll always choose the right one
                //so restarting to simplify code
        }
    }

    public void countryMatchInfoMenu() {
        System.out.println("Please enter a country");
        //should always be valid according to doc, I'll double-check at the end and handle all the error
        String country = this.scanner.nextLine();
        try
        {
            String querySQL = "SELECT A.COUNTRY1, A.COUNTRY2, A.DATE, A.ROUND, B.goals1, C.goals2, D.SALE\n" +
                    "FROM (SELECT M.MATCHID, PC.COUNTRY1, PC.COUNTRY2, M.DATE, M.ROUND\n" +
                    "      FROM (SELECT c1.MATCHID, c1.COUNTRY as COUNTRY1, c2.COUNTRY as COUNTRY2\n" +
                    "            FROM PLAYINGCOUNTRY c1\n" +
                    "                     JOIN PLAYINGCOUNTRY c2 on c1.MATCHID=c2.MATCHID AND c1.COUNTRY NOT LIKE c2.COUNTRY AND c1.COUNTRY < c2.COUNTRY) PC\n" +
                    "               JOIN MATCH M on M.MATCHID = PC.MATCHID\n" +
                    "      WHERE M.MATCHID IN (SELECT M.MATCHID\n" +
                    "                          FROM PLAYINGCOUNTRY pc\n" +
                    "                                   JOIN MATCH M on M.MATCHID = PC.MATCHID\n" +
                    "                          WHERE COUNTRY LIKE \'" + country + "\')\n" +
                    "      ORDER BY M.DATE) as A\n" +
                    "LEFT OUTER JOIN (SELECT g.MATCHID, g.COUNTRY, COUNT(*) as goals1\n" +
                    "      FROM GOAL g\n" +
                    "      group by g.MATCHID, g.COUNTRY) as B on B.MATCHID = A.MATCHID and B.COUNTRY = A.COUNTRY1\n" +
                    "LEFT OUTER JOIN (SELECT g.MATCHID, g.COUNTRY, COUNT(*) as goals2\n" +
                    "      FROM GOAL g\n" +
                    "      group by g.MATCHID, g.COUNTRY) as C on C.MATCHID = A.MATCHID and C.COUNTRY = A.COUNTRY2\n" +
                    "LEFT OUTER JOIN (SELECT t.MATCHID as MATCHID, COUNT(*) as SALE\n" +
                    "      FROM TICKET t\n" +
                    "      WHERE EMAIL IS NOT NULL\n" +
                    "      GROUP BY MATCHID) as D on D.MATCHID = A.MATCHID\n" +
                    "ORDER BY A.DATE;";
//            System.out.println (querySQL) ;
            java.sql.ResultSet rs = statement.executeQuery ( querySQL ) ;

            while (rs.next())
            {
                String country1 = rs.getString ( 1 ) ;
                String country2 = rs.getString ( 2 ) ;
                Date date = rs.getDate (3);
                String round = rs.getString ( 4 ) ;

                //check if in future!
                //if match in future (current date according to ED) then assume goals are null!
                // TODO make sure future match are valid (date far away for grading!)
                // put match in future and adjust other ones!
                // don't know if we need ;...
                Date current = new Date(System.currentTimeMillis());
                //Date current = Date.valueOf("2015-03-31"); to test in the past, but should add match in future
                // so we don't need this!

                //so it can be null
                //could have used Integer, but I wasn't sure how to format it with pretty String.format columns!
                String goal1 ;
                String goal2 ;

                if (date.after(current)) {
                    goal1 = "unknown" ;
                    goal2 = "unknown" ;
                }
                else {
                    goal1 = String.valueOf(rs.getInt(5)) ;
                    goal2 = String.valueOf(rs.getInt(6)) ;
                }
                int seatSold = rs.getInt(7) ;
                System.out.println (String.format("%-15s%-15s%-15s%-15s%-15s%-15s%-15d",country1,country2,date.toString(),round,goal1,goal2,seatSold));
            }
        }
        catch (SQLException e)
        {
            sqlCode = e.getErrorCode(); // Get SQLCODE
            sqlState = e.getSQLState(); // Get SQLSTATE

            // Your code to handle errors comes here;
            // something more meaningful than a print would be good
            System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
            System.out.println(e);
        }

        boolean validOption = false;

        while (!validOption) {
            System.out.println("Enter [A] to find matches of another country, [P] to go to the previous menu:");
            String input = this.scanner.nextLine();
            switch (input) {
                case "A":
                    validOption = true;
                    countryMatchInfoMenu();
                case "P":
                    validOption = true;
                    break;
                default:
                    System.out.println("Invalid Option:((");
                    System.out.println("Valid Options: [A, P]");
                    // countryMatchInfoMenu(); //just repeating as default
                    // but since user always gives proper input, we should never have to get here...
                    //I'll add better error handling later!
            }
        }
    }

    public void insertPlayerMenu() {
        //don't list if country unknown
        //TODO match within 3 days and unknown teams! not shown!
        try
        {

            //check not null!
            String querySQL = "SELECT M.MATCHID, PC.COUNTRY1, PC.COUNTRY2, M.DATE, M.ROUND\n" +
                    "FROM (SELECT c1.MATCHID as MATCHID1, c2.MATCHID as MATCHID2, c1.COUNTRY as COUNTRY1, c2.COUNTRY as COUNTRY2\n" +
                    "      FROM PLAYINGCOUNTRY c1\n" +
                    "         FULL OUTER JOIN PLAYINGCOUNTRY c2 on c1.MATCHID=c2.MATCHID AND c1.COUNTRY NOT LIKE c2.COUNTRY AND c1.COUNTRY < c2.COUNTRY) PC\n" +
                    "         JOIN MATCH M on (MATCHID1 IS NOT NULL AND MATCHID2 IS NOT NULL AND m.MATCHID = MATCHID1)\n" +
                    "WHERE timestamp(m.DATE, m.STARTTIME) < CURRENT TIMESTAMP + 3 DAYS AND timestamp(m.DATE, m.STARTTIME) > CURRENT TIMESTAMP\n" +
                    "ORDER BY M.DATE";
//            System.out.println (querySQL) ;
            java.sql.ResultSet rs = statement.executeQuery ( querySQL ) ;
            System.out.println ("CURRENT TIME: " + LocalDateTime.now());
            System.out.println ("Matches:");
            while ( rs.next ( ) )
            {
                int matchId = rs.getInt ( 1 ) ;
                String country1 = rs.getString (2);
                String country2 = rs.getString (3);
                Date date = rs.getDate (4);
                String round = rs.getString (5);
                System.out.println ("\t"+String.format("%-15s%-15s%-15s%-15s%-15s",matchId,country1,country2,date.toString(),round));
            }

            System.out.println ("Please input match identifier for insert, or [P] to go to the previous menu");
            String matchId = this.scanner.nextLine();
            if (matchId.equals("P")) return;
            System.out.println ("Please input match Country for insert, or [P] to go to the previous menu");
            String country = this.scanner.nextLine();
            if (country.equals("P")) return;
            insertPlayer(matchId, country);

        }
        catch (SQLException e)
        {
            sqlCode = e.getErrorCode(); // Get SQLCODE
            sqlState = e.getSQLState(); // Get SQLSTATE

            // Your code to handle errors comes here;
            // something more meaningful than a print would be good
            System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
            System.out.println(e);
        }
        insertPlayerMenu();
    }

    public void insertPlayer(String matchId, String country) {
        try
        {
            //display players already entered
            System.out.println ("The following players from " + country + " are already entered for match " + matchId + ":");
            String queryEnteredPlayerSQL = "SELECT P.MNAME, P.SHIRTNUMBER, M.ENTRANCETIME, M.EXITTIME, M.YELLOWCARDCOUNT, M.RECEIVEDREDCARD, M.POSITION\n" +
                    "FROM MATCHPLAYER M\n" +
                    "JOIN PLAYER P on M.MEMBERID = P.MEMBERID\n" +
                    "WHERE P.COUNTRY LIKE \'" + country + "\' AND M.MATCHID = " + matchId + "\n" +
                    "ORDER BY P.SHIRTNUMBER";
            java.sql.ResultSet enteredPlayerRs = statement.executeQuery ( queryEnteredPlayerSQL ) ;

            int matchPlayerCounter = 0;
            while ( enteredPlayerRs.next ( ) )
            {
                matchPlayerCounter++;
                String name = enteredPlayerRs.getString (1);
                int shirtNumber = enteredPlayerRs.getInt ( 2 ) ;
                Time entranceTime = enteredPlayerRs.getTime ( 3 ) ;
                Time exitTime = enteredPlayerRs.getTime ( 4 ) ;
                int yellowCardCount = enteredPlayerRs.getInt ( 5 ) ;
                int redCardCount = enteredPlayerRs.getInt ( 6 ) ;
                String position = enteredPlayerRs.getString ( 7 ) ;

                //since it could be null
                String exitTimeString;
                if (exitTime==null) {
                    exitTimeString = "Null";
                }
                else {
                    exitTimeString = exitTime.toString();
                }

                System.out.println("\t"+String.format("%-25s%-5d%-25s from minute %-8s to minute %-15s%-5d%-5d",name,shirtNumber,position,entranceTime.toString(),exitTimeString,yellowCardCount,redCardCount));
                //make sure if future match, then null
            }

            //display available players
            System.out.println ("Possible players from " + country + " not yet selected:");
            String queryAvailablePlayerSQL = "SELECT P.MNAME, P.SHIRTNUMBER, P.GENERALPOSITION, P.MEMBERID\n" +
                    "FROM PLAYER P\n" +
                    "WHERE P.COUNTRY = \'" + country + "\'\n" +
                    "AND P.MEMBERID NOT IN (SELECT M.MEMBERID\n" +
                    "                       FROM MATCHPLAYER M\n" +
                    "                                JOIN PLAYER P on M.MEMBERID = P.MEMBERID\n" +
                    "                       WHERE P.COUNTRY LIKE \'" + country + "\' AND M.MATCHID = " + matchId + ")";
            java.sql.ResultSet availablePlayerRs = statement.executeQuery ( queryAvailablePlayerSQL ) ;

            int optionCounter = 0;
            ArrayList<Integer> availablePlayers = new ArrayList<>();
            while ( availablePlayerRs.next ( ) )
            {
                String name = availablePlayerRs.getString (1);
                int shirtNumber = availablePlayerRs.getInt ( 2 ) ;
                String generalPosition = availablePlayerRs.getString ( 3 ) ;
                availablePlayers.add(availablePlayerRs.getInt ( 4 ));

                System.out.println ("\t"+String.format("%-3d%-25s%-5d%-15s",++optionCounter,name,shirtNumber,generalPosition));
            }

            //max player warning
            if (matchPlayerCounter >= 11) {
                System.out.println ("Maximum number of player for " + country + " reached. Press any key to return to previous menu");
                String wait = scanner.nextLine();
                return;
            }

            System.out.println ("Enter the number of the player you want to insert or [P] to go to the previous menu");
            String choice = this.scanner.nextLine();
            if (choice.equals("P")) return;
            int insertPlayerNumber = Integer.parseInt(choice);
            int insertPlayerId = availablePlayers.get(insertPlayerNumber-1);
            System.out.println ("Enter the player's position");
            String insertPlayerPosition = this.scanner.nextLine();

            //insert here
            String updateSQL = "INSERT INTO MATCHPLAYER (matchId, memberId, position, entranceTime, exitTime, yellowCardCount, receivedRedCard) " +
                    "VALUES (" + matchId + ", " + insertPlayerId + ", \'" + insertPlayerPosition + "\', '00:00:00', null, 0, 0)";
            statement.executeUpdate(updateSQL);

            //repeat
            //max 11 players
            insertPlayer(matchId, country);

        }
        catch (SQLException e)
        {
            sqlCode = e.getErrorCode(); // Get SQLCODE
            sqlState = e.getSQLState(); // Get SQLSTATE

            // Your code to handle errors comes here;
            // something more meaningful than a print would be good
            System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
            System.out.println(e);
        }
    }

    public void insertGoalMenu() {
        System.out.println("Input match identifier for insert, or [P] to go to the previous menu");
        String choice = scanner.nextLine();
        if (choice.equals("P")) return;

        int matchId = Integer.parseInt(choice);
        insertGoal(matchId);

    }

    public void insertGoal(int matchId) {
        try
        {
            int occurrenceCounter = 1;
            System.out.println("Current goals:");
            String queryGoalsSQL = "SELECT G.OCCURRENCE, G.COUNTRY, P.MNAME, G.TIME_STAMP, G.ISPENALTY\n" +
                    "FROM GOAL G\n" +
                    "Join PLAYER P on G.MEMBERID = P.MEMBERID\n" +
                    "WHERE MATCHID = " + matchId +
                    " ORDER BY G.OCCURRENCE";
            java.sql.ResultSet goalsRs = statement.executeQuery ( queryGoalsSQL ) ;
            while ( goalsRs.next ( ) )
            {
                occurrenceCounter++;
                int occurence = goalsRs.getInt(1);
                String country = goalsRs.getString(2);
                String name = goalsRs.getString(3);
                Time timestamp = goalsRs.getTime(4);
                Boolean isPenalty = goalsRs.getBoolean(5);
                String info;
                if (isPenalty) {
                    info = "Penalty kick";
                }
                else {
                    info = timestamp.toString();
                }


                System.out.println ("\t"+String.format("%-3d%-15s%-25s%-15s",occurence,country,name, info));
            }

            System.out.println("Match player for match " + matchId + ":");

            String queryMatchPlayerSQL = "SELECT P.COUNTRY, P.MNAME, M.MEMBERID\n" +
                    "FROM MATCHPLAYER M\n" +
                    "JOIN PLAYER P on M.MEMBERID = P.MEMBERID\n" +
                    "WHERE M.MATCHID = " + matchId + "\n" +
                    "ORDER BY COUNTRY, MNAME";
            java.sql.ResultSet matchPlayerRs = statement.executeQuery ( queryMatchPlayerSQL ) ;

            int optionCounter = 0;
            ArrayList<Integer> matchPlayers = new ArrayList<>();
            ArrayList<String> countries = new ArrayList<>();

            while ( matchPlayerRs.next ( ) )
            {
                String country = matchPlayerRs.getString (1);
                countries.add(country);
                String name = matchPlayerRs.getString ( 2 ) ;
                matchPlayers.add(matchPlayerRs.getInt ( 3 ));

                System.out.println ("\t"+String.format("%-3d%-15s%-25s",++optionCounter,country,name));
            }

            System.out.println("Select player for insert or [P] to go to the previous menu");
            String choice = scanner.nextLine();
            if (choice.equals("P")) return;
            int matchPlayerNumber = Integer.parseInt(choice);
            int matchPlayerId = matchPlayers.get(matchPlayerNumber-1);
            String country = countries.get(matchPlayerNumber-1);

            System.out.println("Was it a penalty kick? [Y/N] or [P] to go to the previous menu");
            while (true) {
                choice = scanner.nextLine();
                switch (choice)
                {
                    case "P":
                        return;
                    case "Y":
                        String addPenaltyGoalSQL = "INSERT INTO Goal (matchId, occurrence, country, memberId, time_stamp, isPenalty)" +
                                "VALUES (" + matchId + ", " + occurrenceCounter + ", \'" + country + "\', " + matchPlayerId + ", null, 1)";
                        statement.executeUpdate(addPenaltyGoalSQL);
                        insertGoal(matchId);
                        return;
                    case "N":
                        System.out.println("Enter timestamp in format [00:00:00] or [P] to go to the previous menu");
                        String timestamp = scanner.nextLine();
                        if (timestamp.equals("P")) return;
                        String addGoalSQL = "INSERT INTO Goal (matchId, occurrence, country, memberId, time_stamp, isPenalty)\n" +
                                "VALUES (" + matchId + ", " + occurrenceCounter + ", '" + country + "', " + matchPlayerId + ", \'" + timestamp + "\', 0)";
                        statement.executeUpdate(addGoalSQL);
                        insertGoal(matchId);
                        return;
                    default:
                        System.out.println("Invalid option! please select [P], [Y] or [N]");
                }
            }
        }
        catch (SQLException e)
        {
            sqlCode = e.getErrorCode(); // Get SQLCODE
            sqlState = e.getSQLState(); // Get SQLSTATE

            // Your code to handle errors comes here;
            // something more meaningful than a print would be good
            System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
            System.out.println(e);
        }
    }
}

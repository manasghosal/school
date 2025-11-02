using System;
using System.Collections.Generic;
using System.Data.SqlClient;
using System.Linq;
using System.Threading.Tasks;
using System.Web;

namespace SchoolApi.DBAccess
{
    public class SchoolSQLServer
    {
        //Data Source = AGSL - PC; Initial Catalog = MainSchool; Persist Security Info=True;User ID = sa; Password=Passw0rd
        public async Task<SqlConnection> ConnectDB()
        {
            // SqlConnection con = new SqlConnection("Data Source = 43.255.152.26; Initial Catalog = MainSchool; "
            //         + "Persist Security Info = True; User ID = User_MainSchool; Password = MainSchool123*0");
            //SqlConnection con = new SqlConnection("Server = AGSL-PC; Database = MainSchool; "
            //        + " User Id = sa; Password = Passw0rd");

            SqlConnection con = new SqlConnection("Data Source = DESKTOP-RNGA34P; Initial Catalog = MainSchool; Persist Security Info = True;  User ID = sa; Password = Passw0rd");
            await con.OpenAsync();
            return con;
        }
        public async Task<SqlDataReader> ReadData(SqlCommand cmd)
        {
            SqlDataReader reader = null;
            try
            {
                reader = await cmd.ExecuteReaderAsync();

            }
            catch (Exception ex) { return null; }
            return reader;
        }
        public void CloseReader(SqlDataReader reader)
        {
            reader.Close();
        }
        public void CloseDB(SqlConnection con)
        {
            con.Close();
        }
    }
}
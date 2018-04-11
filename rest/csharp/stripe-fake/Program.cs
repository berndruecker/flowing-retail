using FlowingRetailPayment.Controllers;
using Microsoft.AspNetCore;
using Microsoft.AspNetCore.Hosting;
using System;
using System.Threading;

namespace FlowingRetailPayment
{
    public class Program
    {

        public static void Main(string[] args)
        {
            new Thread(() =>
            {
                //Thread.CurrentThread.IsBackground = true;
                
                Console.WriteLine("Service is operating normal");
                bool running = true;

                while (running)
                {
                    Console.WriteLine("[S]low, [N]ormal, E[X]it: ");
                    char mode = Char.ToUpper(Console.ReadKey().KeyChar);
                    if ('S'.Equals(mode))
                    {
                        StripeFakeController.slow = true;
                        Console.WriteLine("Service is now slow");
                    }
                    else if ('N'.Equals(mode))
                    {
                        StripeFakeController.slow = false;
                        Console.WriteLine("Service is back to normal");
                    }
                    else if ('X'.Equals(mode))
                    {
                        running = false;
                    }
                }

            }).Start();

            BuildWebHost(args).Run();
        }

        public static IWebHost BuildWebHost(string[] args) =>
            WebHost.CreateDefaultBuilder(args)
                .UseStartup<Startup>()
                .UseUrls("http://localhost:8099")
                .Build();
    }
}

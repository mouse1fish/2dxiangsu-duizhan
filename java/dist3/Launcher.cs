using System;
using System.Diagnostics;
using System.IO;
using System.Reflection;
using Microsoft.Win32;

[assembly: AssemblyTitle("PixelBattle")]
[assembly: AssemblyProduct("PixelBattle")]
[assembly: AssemblyVersion("1.0.0.0")]

class Program
{
    static string FindJavaw()
    {
        string javaHome = Environment.GetEnvironmentVariable("JAVA_HOME");
        if (!string.IsNullOrEmpty(javaHome))
        {
            string p = Path.Combine(javaHome, "bin", "javaw.exe");
            if (File.Exists(p)) return p;
        }

        string pathVar = Environment.GetEnvironmentVariable("PATH") ?? "";
        foreach (string dir in pathVar.Split(Path.PathSeparator))
        {
            string p = Path.Combine(dir.Trim(), "javaw.exe");
            if (File.Exists(p)) return p;
        }

        string[] commonDirs = new string[]
        {
            @"C:\Program Files\Java\jdk-21",
            @"C:\Program Files\Java\jdk-20",
            @"C:\Program Files\Java\jdk-19",
            @"C:\Program Files\Java\jdk-17",
            @"C:\Program Files\Eclipse Adoptium\jdk-21",
            @"C:\Program Files\Eclipse Adoptium\jdk-17",
            @"C:\Program Files\Microsoft\jdk-21",
            @"C:\Program Files\Microsoft\jdk-17",
        };
        foreach (string d in commonDirs)
        {
            string p = Path.Combine(d, "bin", "javaw.exe");
            if (File.Exists(p)) return p;
        }

        try
        {
            using (RegistryKey key = Registry.LocalMachine.OpenSubKey(@"SOFTWARE\JavaSoft\Java Runtime Environment"))
            {
                if (key != null)
                {
                    string[] subs = key.GetSubKeyNames();
                    if (subs.Length > 0)
                    {
                        using (RegistryKey latest = key.OpenSubKey(subs[subs.Length - 1]))
                        {
                            object val = latest.GetValue("JavaHome");
                            string javaHome2 = val as string;
                            if (!string.IsNullOrEmpty(javaHome2))
                            {
                                string p = Path.Combine(javaHome2, "bin", "javaw.exe");
                                if (File.Exists(p)) return p;
                            }
                        }
                    }
                }
            }
        }
        catch { }

        return null;
    }

    static void Main()
    {
        string dir = AppDomain.CurrentDomain.BaseDirectory;
        string jar = Path.Combine(dir, "PixelBattle.jar");

        if (!File.Exists(jar))
        {
            Console.WriteLine("PixelBattle.jar not found!");
            Console.ReadLine();
            return;
        }

        string javaw = FindJavaw();
        if (javaw == null)
        {
            Console.WriteLine("============================================");
            Console.WriteLine("  PixelBattle - Java not found!");
            Console.WriteLine("  Please install JDK 17+ and try again.");
            Console.WriteLine("  Download: https://adoptium.net/");
            Console.WriteLine("============================================");
            Console.ReadLine();
            return;
        }

        Process.Start(new ProcessStartInfo
        {
            FileName = javaw,
            Arguments = "-Xmx512m -jar \"" + jar + "\"",
            UseShellExecute = false,
            CreateNoWindow = true
        });
    }
}

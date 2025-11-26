@echo off
echo ====================================
echo   Platform Migration for Orders
echo ====================================
echo.
echo This will add the 'platform' column to your orders table
echo Please make sure PostgreSQL is running and you have the correct connection details
echo.
pause

echo.
echo Running platform migration...
echo.

REM Replace these with your actual database connection details:
REM -h localhost = your database host
REM -p 5432 = your database port  
REM -U your_username = your PostgreSQL username
REM -d your_database_name = your database name

psql -h localhost -p 5432 -U your_username -d your_database_name -f platform_migration.sql

echo.
echo Migration completed!
echo.
pause
